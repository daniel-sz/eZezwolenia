package org.szewczyk.pwr.pzwmanager.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
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
import org.szewczyk.pwr.pzwmanager.service.OrderService;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
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
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        Cart cart = cartService.findBySessionId(currentSessionId);

        order.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm:ss.AAA")));
        order.getOrderItems().addAll(cart.getOrderedItems());
        order.setValue(cart.getSumPrice());
        order.setOrderNumber();
        order.setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        order.setStatus(Order.Status.PENDING);

        cart.getOrderedItems().clear();
        cartService.deleteCart(cart);


        String accessToken = getToken();        // get PayU Oauth2 token
        Map<String, String> response = createPayUOrder(order, accessToken);
        String redirectAddress = response.get("redirectUri");    // create PayU order
//        System.out.println("LOG -------------> ExtOrderId: " + order.getOrderNumber());
//        System.out.println("LOG -------------> TOKEN: " + accessToken);
//        System.out.println("LOG -------------> OrderID: " + response.get("orderId"));
        order.setPayuOrderId(response.get("orderId"));
        orderService.saveOrder(order);

        if (redirectAddress != null){
            return ("redirect:" + redirectAddress);
        }

        return "redirect:";
    }

    @PostMapping(value = "notify")
    public ModelAndView orderStatus(){
        String token = getToken();
        ModelAndView modelAndView = new ModelAndView();
        HttpResponse<JsonNode> jsonResponse = Unirest.get(ORDER_URL + orderService.findAll().get(orderService.findAll().size() - 1).getPayuOrderId())
                .header("Authorization", "Bearer " + token)
                .asJson();
        String status = jsonResponse.getBody().getObject().getJSONObject("status").getString("statusCode");
        String orderNum = jsonResponse.getBody().getObject().getJSONArray("orders").getJSONObject(0).getString("extOrderId");
        String payuOrderId = jsonResponse.getBody().getObject().getJSONArray("properties").getJSONObject(0).getString("value");

        if (status.equals("SUCCESS")){
            System.out.println("----- PAYMENT no. " + payuOrderId + " SUCCESS!!! -----");
            Order o = orderService.findByOrderNum(orderNum);
            o.setStatus(Order.Status.SUCCESS);
            orderService.saveOrder(o);
        }
        modelAndView.addObject("item", jsonResponse.getBody());
        modelAndView.setViewName("finalizeOrder");
        return modelAndView;
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
        CompletableFuture<HttpResponse<JsonNode>> jsonResponse = Unirest.post(ORDER_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"notifyUrl\": \"https://e-zezwolenia.herokuapp.com/cart/notify\", " +
                        "\"customerIp\": \"127.0.0.1\", " +
                        "\"merchantPosId\": \"" + CLIENT_ID + "\", " +
                        "\"description\": \"Platnosc za pozwolenie\", " +
                        "\"currencyCode\": \"PLN\", " +
                        "\"totalAmount\": \"" + order.getValue().movePointRight(2).toString() + "\", " +
                        "\"extOrderId\": \"" + order.getOrderNumber() + "\", " +
                        "\"buyer\": {" +
                            "\"email\": \"" + order.getEmail() + "\", " +
//                            "\"phone\": \"654111654\", " +
                            "\"firstName\": \"" + order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getFirstName() + "\", " +
                            "\"lastName\": \"" + order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getLastName() + "\" }," +
                        "\"products\": [{" +
                            "\"name\": \"Zamowienie nr " + order.getOrderNumber() + "\", " +
                            "\"unitPrice\": \"" + order.getValue().movePointRight(2).toString() + "\", " +
                            "\"quantity\": \"1\"}]}")
                .asJsonAsync(httpResponse -> {
                    int code = httpResponse.getStatus();
                    JsonNode body = httpResponse.getBody();
//                    System.out.println("Code: " + code);
//                    System.out.println("Body: " + body);
                });

        Map<String, String> orderDetails = new HashMap<>();
        try {
            System.out.println(jsonResponse.get().getBody().getObject().toString());
            orderDetails.put("redirectUri", jsonResponse.get().getBody().getObject().getString("redirectUri"));
            orderDetails.put("orderId", jsonResponse.get().getBody().getObject().getString("orderId"));
        } catch (Exception e){
            e.printStackTrace();
            orderDetails.put("redirectUri", "");
            orderDetails.put("orderId", "null");
        }
        return orderDetails;
    }


    private void pdfInvoiceGenerator(Order order){
        File tempDir = new File("." + File.separator + "tempInvoices");
        if (!tempDir.exists() && !tempDir.isDirectory()) tempDir.mkdir();

        File template = new File(tempDir + File.separator + "invoiceTemplate.tex");
    }
}

