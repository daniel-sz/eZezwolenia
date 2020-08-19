package org.szewczyk.pwr.pzwmanager.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.Cart;
import org.szewczyk.pwr.pzwmanager.model.Order;
import org.szewczyk.pwr.pzwmanager.service.CartService;
import org.szewczyk.pwr.pzwmanager.service.MailService;
import org.szewczyk.pwr.pzwmanager.service.OrderService;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
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

    private final String CLIENT_ID = "391607";
    private final String CLIENT_SECRET = "7ed04dd6f7ff71a7b5f010b759b575d0";
    private final String AUTH_URL = "https://secure.snd.payu.com/pl/standard/user/oauth/authorize";
    private final String ORDER_URL = "https://secure.snd.payu.com/api/v2_1/orders/";
//    private final String HOME_URL = "http://localhost:8888";
    private final String HOME_URL = "https://e-zezwolenia.herokuapp.com";

    private final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };


    @GetMapping(value = "")
    public ModelAndView openCart(){
        ModelAndView modelAndView = new ModelAndView("cart");
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        Cart cartExists = cartService.findBySessionId(currentSessionId);
        if (cartExists != null){
            modelAndView.addObject("cart", cartExists);
        } else {
            Cart newCart = new Cart();
            newCart.setSessionId(currentSessionId);
            modelAndView.addObject("cart", newCart);
            cartService.saveCart(newCart);
        }

        return modelAndView;
    }

    private String getUsersIp(RequestAttributes requestAttributes){
        if (requestAttributes == null) return "0.0.0.0";
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        for (String header : IP_HEADER_NAMES){
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)){
                return ipList.split(",")[0];
            }
        }
        return request.getRemoteAddr();
    }
    private String getToken(){
        HttpResponse<JsonNode> jsonResponse = Unirest.post(AUTH_URL)
                .field("grant_type", "client_credentials")
                .field("client_id", CLIENT_ID)
                .field("client_secret", CLIENT_SECRET)
                .asJson();
        return jsonResponse.getBody().getObject().getString("access_token");
    }
    private Map<String, String> createPayUOrder(Order order, String token){
//        JSON PAYLOAD
        JSONObject payload = new JSONObject();
//        payload.put("notifyUrl", "https://e-zezwolenia.herokuapp.com/cart/notify?orderId=" + order.getOrderNumber());
        payload.put("continueUrl", HOME_URL + "/cart/orderDetails?orderId=" + order.getOrderNumber());
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
//                    System.out.println(httpResponse.getBody());
                });

        Map<String, String> orderDetails = new HashMap<>();
        try {
            orderDetails.put("redirectUri", jsonResponse.get().getBody().getObject().getString("redirectUri"));
            orderDetails.put("orderId", jsonResponse.get().getBody().getObject().getString("orderId"));
        } catch (Exception e){
            e.printStackTrace();
            orderDetails.put("redirectUri", "");
            orderDetails.put("orderId", "null");
        }
        return orderDetails;
    }

    @RequestMapping(value = "finalizeOrder")
    public String finalizeOrder(Order order){
//        ------------ GET USER'S IP
        System.out.println("IP uzytkownika: " + getUsersIp(RequestContextHolder.currentRequestAttributes()));

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

    @GetMapping(value = "orderDetails")
    public ModelAndView orderStatus(@RequestParam String orderId){
        String token = getToken();
        Order order = orderService.findByOrderNum(orderId);
        ModelAndView modelAndView = new ModelAndView();
        if (order == null){
            return null;
        } else {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(ORDER_URL + order.getPayuOrderId())
                    .header("Authorization", "Bearer " + token)
                    .asJson();
            String status = jsonResponse.getBody().getObject().getJSONArray("orders").getJSONObject(0).getString("status");
            String orderNum = jsonResponse.getBody().getObject().getJSONArray("orders").getJSONObject(0).getString("extOrderId");
            String payuPaymentId = jsonResponse.getBody().getObject().getJSONArray("properties").getJSONObject(0).getString("value");

            if (status.equals("COMPLETED")) {
                if (orderId.equals(orderNum) && order.getStatus().equals(Order.Status.PENDING)) {
                    order.setStatus(Order.Status.SUCCESS);
                    order.setPayuPaymentId(payuPaymentId);
                    orderService.saveOrder(order);

//                EMAIL SENDING
                    String mailAddress = order.getEmail();
                    String subject = "Potwierdzenie zamówienia " + order.getOrderNumber() + " i płatności nr " + payuPaymentId;
                    String mailText =
                            """
                                    Witamy w serwisie e-Zezwolenia,

                                    W załączniku będzie znajdował się plik z potwierdzeniem zamówienia.
                                    Zapraszamy z tym plikiem (w wersji elektronicznej lub wydrukowanej) do zarządu PZW po odbiór wymaganych naklejek.
                                    Dziękujemy za skorzystanie z naszych usług.

                                    Polski Związek Wędkarski
                                    Okręg w Wałbrzychu
                                    Koło Kudowa Zdrój""";
                    try {
                        mailService.sendMail(mailAddress, subject, mailText, false, order);
                    } catch (MessagingException e) {
                        System.out.println(" - Wyjątek podczas wysyłania maila - ");
                        e.printStackTrace();
                    }
                }
            }
            modelAndView.addObject("order", order);
            modelAndView.addObject("item", jsonResponse.getBody());
            modelAndView.setViewName("finalizeOrder");
            return modelAndView;
        }
    }
}

