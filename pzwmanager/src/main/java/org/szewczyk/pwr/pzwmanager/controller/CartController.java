package org.szewczyk.pwr.pzwmanager.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.Cart;
import org.szewczyk.pwr.pzwmanager.model.Order;
import org.szewczyk.pwr.pzwmanager.service.CartService;
import org.szewczyk.pwr.pzwmanager.service.OrderService;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/cart")
public class CartController {
    @Resource
    private CartService cartService;
    @Resource
    private OrderService orderService;

    final String CLIENT_ID = "391888";
    final String CLIENT_SECRET = "486a8f96791e5b64dba56e8154c480b9";
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
    public ModelAndView finalizeOrder(Order order){
        ModelAndView modelAndView = new ModelAndView();
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        Cart cart = cartService.findBySessionId(currentSessionId);

        order.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        order.getOrderItems().addAll(cart.getOrderedItems());
        order.setValue(cart.getSumPrice());
        order.setOrderNumber((orderService.findAll().size() > 0) ? (orderService.findAll().get(orderService.findAll().size() - 1).getId() + 1) : 1 );
        order.setStatus(Order.Status.PENDING);
        orderService.saveOrder(order);

        cart.getOrderedItems().clear();
        cartService.deleteCart(cart);


        String accessToken = getToken();        // get PayU Oauth2 token
        String redirectAddress = createPayUOrder(order, accessToken).get("redirectUri");    // create PayU order


        if (redirectAddress != null){
            return new ModelAndView("redirect:" + redirectAddress);
        }

        modelAndView.addObject("orderDetails", order);
        modelAndView.setViewName("finalizeOrder");
        return modelAndView;
    }

    @GetMapping(value = "notify")
    public ModelAndView orderStatus(String orderId, String token){
        ModelAndView modelAndView = new ModelAndView();
        HttpResponse<JsonNode> jsonResponse = Unirest.get(ORDER_URL + orderId)
                .header("Authorization", "Bearer " + token)
                .asJson();
        System.out.println(jsonResponse.getBody());
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
        String accessToken = jsonResponse.getBody().getObject().getString("access_token");
//        System.out.println("Access token: " + accessToken);
        return accessToken;
    }
    private Map<String, String> createPayUOrder(Order order, String token){
        HttpResponse<JsonNode> jsonResponse = Unirest.post(ORDER_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"notifyUrl\": \"https://localhost:8888/notify\", " +
                        "\"customerIp\": \"127.0.0.1\", " +
                        "\"merchantPosId\": \"391888\", " +
                        "\"description\": \"Platnosc za pozwolenie\", " +
                        "\"currencyCode\": \"PLN\", " +
                        "\"totalAmount\": \"" + order.getValue().movePointRight(2).toString() + "\", " +
                        "\"extOrderId\": \"" + order.getOrder_number() + "\", " +
                        "\"buyer\": {" +
                            "\"email\": \"" + order.getEmail() + "\", " +
//                            "\"phone\": \"654111654\", " +
                            "\"firstName\": \"" + order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getFirstName() + "\", " +
                            "\"lastName\": \"" + order.getOrderItems().get(order.getOrderItems().size() - 1).getPerson().getLastName() + "\" }," +
                        "\"products\": [{" +
                            "\"name\": \"Zamówienie nr " + order.getOrder_number() + "\", " +
                            "\"unitPrice\": \"" + order.getValue().movePointRight(2).toString() + "\", " +
                            "\"quantity\": \"1\"}]}")
                .asJson();

        Map<String, String> orderDetails = new HashMap<>();
        try {
            orderDetails.put("redirectUri", jsonResponse.getBody().getObject().getString("redirectUri"));
            orderDetails.put("orderId", jsonResponse.getBody().getObject().getString("orderId"));
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

