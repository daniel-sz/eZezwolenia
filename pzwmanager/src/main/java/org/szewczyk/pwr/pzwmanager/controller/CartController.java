package org.szewczyk.pwr.pzwmanager.controller;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.aspectj.weaver.ast.Or;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Controller
@RequestMapping(value = "/cart")
public class CartController {
    @Resource
    private CartService cartService;
    @Resource
    private OrderService orderService;

    final String CLIENT_ID = "391888";
    final String CLIENT_SECRET = "486a8f96791e5b64dba56e8154c480b9";
//    final String CLIENT_ID = "145227";
//    final String CLIENT_SECRET = "12f071174cb7eb79d4aac5bc2f07563f";
    final String TARGET_URI = "https://secure.snd.payu.com/pl/standard/user/oauth/authorize";


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
        Order placedOrder = new Order();

        order.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        order.setOrderNumber();
        order.getOrderItems().addAll(cart.getOrderedItems());
        cart.getOrderedItems().clear();
//        cartService.deleteCart(cart);
        order.setValue(cart.getSumPrice());
        orderService.saveOrder(order);
        order.setOrderNumber();
        orderService.deleteById(order.getId());
        orderService.saveOrder(order);

        getToken();

        System.out.println("----> Zamówienie nr: " + order.getOrder_number() + " o wartości: " + order.getValue()
                + " na adres: " + order.getEmail());
        modelAndView.addObject("orderDetails", order);
        modelAndView.setViewName("finalizeOrder");
        return modelAndView;
    }

    private String getToken(){
        String amount = orderService.findAll().get(orderService.findAll().size() - 1).getValue().movePointRight(2).toPlainString();
        System.out.println("PayU amount: " + amount);

        HttpResponse<JsonNode> jsonResponse = Unirest.post(TARGET_URI)
                .header("Content-Type", "application/json")
                .field("grant_type", "client_credentials")
                .field("client_id", CLIENT_ID)
                .field("client_secret", CLIENT_SECRET)
                .asJson();

        System.out.println(jsonResponse.getStatus() + "\n" + jsonResponse.getBody());
        return "redirect:/";
    }

    private void pdfInvoiceGenerator(Order order){
        File tempDir = new File("." + File.separator + "tempInvoices");
        File template = new File(tempDir + File.separator + "invoiceTemplate.tex");
    }
}

