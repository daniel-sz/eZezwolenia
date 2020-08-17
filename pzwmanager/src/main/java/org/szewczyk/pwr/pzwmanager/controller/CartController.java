package org.szewczyk.pwr.pzwmanager.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.Cart;
import org.szewczyk.pwr.pzwmanager.model.Order;
import org.szewczyk.pwr.pzwmanager.service.CartService;
import org.szewczyk.pwr.pzwmanager.service.MailService;
import org.szewczyk.pwr.pzwmanager.service.OrderService;
import org.szewczyk.pwr.pzwmanager.service.PDFService;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping(value = "/cart")
public class CartController {
    @Resource
    private CartService cartService;
    @Resource
    private OrderService orderService;
    @Resource
    private MailService mailService;
    @Resource
    private PDFService pdfService;


    final String CLIENT_ID = "391607";
    final String CLIENT_SECRET = "7ed04dd6f7ff71a7b5f010b759b575d0";
    final String AUTH_URL = "https://secure.snd.payu.com/pl/standard/user/oauth/authorize";
    final String ORDER_URL = "https://secure.snd.payu.com/api/v2_1/orders/";


    @GetMapping(value = "")
    public ModelAndView openCart(){
        ModelAndView modelAndView = new ModelAndView("cart");
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        System.out.println("Current Session ID ----------------> " + currentSessionId);
        Cart cartExists = cartService.findBySessionId(currentSessionId);
        if (cartExists != null){
            modelAndView.addObject("cart", cartExists);
        } else {
            Cart newCart = new Cart();
            newCart.setSessionId(currentSessionId);
            modelAndView.addObject("cart", newCart);
            cartService.saveCart(newCart);
        }
        Order newOrder = new Order();
        newOrder.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        orderService.saveOrder(newOrder);
        modelAndView.addObject("order", newOrder);
        return modelAndView;
    }

    @RequestMapping(value = "finalizeOrder")
    public String finalizeOrder(Order order){
//        ------------ FIND USER'S CART
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        Cart cart = cartService.findBySessionId(currentSessionId);

//        ------------ SET ORDER DETAILS
        order.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss.AAA")));
        order.getOrderItems().addAll(cart.getOrderedItems());
        order.setValue(cart.getSumPrice());
        order.setOrderNumber();
        order.setStatus(Order.Status.PENDING);
        orderService.saveOrder(order);
        System.out.println("Order number before creating payment: " + order.getOrderNumber());

//        ------------ CLEAR CART
        cart.getOrderedItems().clear();
        cart.setSumPrice(BigDecimal.ZERO);
        cartService.deleteCart(cart);

//        ------------ CREATE PAYMENT
        String accessToken = getToken();        // get PayU Oauth2 token
        Map<String, String> response = createPayUOrder(order, accessToken);
        String redirectAddress = response.get("redirectUri");    // create PayU order

//        ------------ UPDATE ORDER DETAILS
        order.setPayuOrderId(response.get("orderId"));
        orderService.saveOrder(order);

        if (redirectAddress != null){
            return ("redirect:" + redirectAddress);
        }

        return "redirect:";
    }

    @PostMapping(value = "orderDetails")
//    @Async
    public ModelAndView orderStatus(@RequestParam String orderId){
//        try {
//            System.out.println("Notify recieved!");
//            System.out.println("Response body: " + response);
//        }catch (Exception ignored){
//
//        }
//        return null;
        String token = getToken();
        Order order = orderService.findByOrderNum(orderId);
        ModelAndView modelAndView = new ModelAndView();
        if (order == null){
            return null;
        } else {
            System.out.println("PayU Order ID: " + order.getPayuOrderId());
            HttpResponse<JsonNode> jsonResponse = Unirest.get(ORDER_URL + order.getPayuOrderId())
                    .header("Authorization", "Bearer " + token)
                    .asJson();
            String status = jsonResponse.getBody().getObject().getJSONArray("orders").getJSONObject(0).getString("status");
            System.out.println("STATUS: " + status);

            String orderNum = jsonResponse.getBody().getObject().getJSONArray("orders").getJSONObject(0).getString("extOrderId");
            String payuOrderId = jsonResponse.getBody().getObject().getJSONArray("properties").getJSONObject(0).getString("value");
            System.out.println("Order num after payment: " + orderNum);

            if (status.equals("COMPLETED")) {
                System.out.println("----- PAYMENT no. " + payuOrderId + " SUCCESS!!! -----");
                Order o = orderService.findByOrderNum(orderNum);
                if (o != null && o.getStatus().equals(Order.Status.PENDING)) {
                    o.setStatus(Order.Status.SUCCESS);
                    orderService.saveOrder(o);
//                PDF GEN


//                EMAIL SENDING
                    String mailAddress = o.getEmail();
                    String subject = "Potwierdzenie zamowienia " + o.getOrderNumber() + " i platnosci nr " + payuOrderId;
                    String mailText =
                            """
                                    Witamy w serwisie e-Zezwolenia,

                                    W załączniku będzie znajdował się plik z potwierdzeniem zamowienia.
                                    Zapraszamy z tym plikiem (w wersji elektronicznej lub wydrukowanej) do zarzadu PZW po odbior wymaganych naklejek.
                                    Dziękujemy za skorzystanie z naszych uslug.

                                    Pozdrawiam
                                    Twórca tego przybytku,
                                    Daniel Szewczyk""";
                    try {
                        mailService.sendMail(mailAddress, subject, mailText, false);
                        System.out.println(" - Mail wysłany - ");
                    } catch (MessagingException e) {
                        System.out.println(" - Wyjątek podczas wysyłania maila - ");
//                e.printStackTrace();
                    }
                }
            }
            modelAndView.addObject("item", jsonResponse.getBody());
            modelAndView.setViewName("finalizeOrder");
            return modelAndView;
        }
    }

    private String getToken(){
        HttpResponse<JsonNode> jsonResponse = Unirest.post(AUTH_URL)
                .field("grant_type", "client_credentials")
                .field("client_id", CLIENT_ID)
                .field("client_secret", CLIENT_SECRET)
                .asJson();
        //        System.out.println("Access token: " + accessToken);
        return jsonResponse.getBody().getObject().getString("access_token");
    }
    private Map<String, String> createPayUOrder(Order order, String token){
//        JSON PAYLOAD
        JSONObject payload = new JSONObject();
//        payload.put("notifyUrl", "https://e-zezwolenia.herokuapp.com/cart/notify?orderId=" + order.getOrderNumber());
        payload.put("continueUrl", "https://e-zezwolenia.herokuapp.com/cart/orderDetails?orderId=" + order.getOrderNumber());
        payload.put("customerIp", "127.0.0.1");
        payload.put("merchantPosId", CLIENT_ID);
        payload.put("description", "Platnosc za pozwolenie");
        payload.put("currencyCode", "PLN");
        payload.put("totalAmount", order.getValue().movePointRight(2).toString());
        payload.put("extOrderId", order.getOrderNumber());
            JSONObject buyer = new JSONObject();
            buyer.put("email", order.getEmail());
            buyer.put("firstName", order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getFirstName());
            buyer.put("lastName", order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getLastName());
        payload.put("buyer", buyer);
                JSONObject product1 = new JSONObject();
                product1.put("name", "Zamowienie nr " + order.getOrderNumber());
                product1.put("unitPrice", order.getValue().movePointRight(2).toString());
                product1.put("quantity", 1);
            JSONArray products = new JSONArray();
            products.put(product1);
        payload.put("products", products);

        CompletableFuture<HttpResponse<JsonNode>> jsonResponse = Unirest.post(ORDER_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(payload)
                .asJsonAsync(httpResponse -> {
                    int code = httpResponse.getStatus();
                    JsonNode body = httpResponse.getBody();
                    System.out.println("Code: " + code);
                    System.out.println("Body: " + body);
                });

        Map<String, String> orderDetails = new HashMap<>();
        try {
//            System.out.println(jsonResponse.get().getBody().getObject().toString());
            orderDetails.put("redirectUri", jsonResponse.get().getBody().getObject().getString("redirectUri"));
            orderDetails.put("orderId", jsonResponse.get().getBody().getObject().getString("orderId"));
        } catch (Exception e){
            e.printStackTrace();
            orderDetails.put("redirectUri", "");
            orderDetails.put("orderId", "null");
        }
        return orderDetails;
    }
}

