package org.szewczyk.pwr.pzwmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.*;
import org.szewczyk.pwr.pzwmanager.service.CartService;
import org.szewczyk.pwr.pzwmanager.service.ItemService;
import org.szewczyk.pwr.pzwmanager.service.OrderService;
import org.szewczyk.pwr.pzwmanager.service.PersonService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {
    @Resource
    private PersonService personService;
    @Resource
    private ItemService itemService;
    @Resource
    private CartService cartService;
    @Resource
    private OrderService orderService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping(value = "/")
    public ModelAndView hello(){
        System.out.println(request.getRemoteHost());
        ModelAndView modelAndView = new ModelAndView("main");
        Cart newCart = new Cart();
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
//        System.out.println("Current Session ID ----------------> " + currentSessionId);
        if (cartService.findBySessionId(currentSessionId) == null){
            newCart.setSessionId(currentSessionId);
            cartService.saveCart(newCart);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/dodajTestoweDane")
    public ModelAndView addTestData(){
        ModelAndView modelAndView = new ModelAndView("main");
        itemService.saveItem(
                new Item("Test1", "Pozwolenie testowe", Item.category.MEMBER,
                        BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.0)));
        itemService.saveItem(
                new Item("Test2", "Pozwolenie testowe", Item.category.MEMBER,
                        BigDecimal.valueOf(2.0), BigDecimal.valueOf(4.0), BigDecimal.valueOf(6.0), BigDecimal.valueOf(8.0)));
        itemService.saveItem(
                new Item("Test3", "Pozwolenie testowe", Item.category.MEMBER,
                        BigDecimal.valueOf(3.0), BigDecimal.valueOf(6.0), BigDecimal.valueOf(9.0), BigDecimal.valueOf(12.0)));
        itemService.saveItem(
                new Item("Test4", "Pozwolenie testowe", Item.category.MEMBER,
                        BigDecimal.valueOf(4.0), BigDecimal.valueOf(8.0), BigDecimal.valueOf(12.0), BigDecimal.valueOf(16.0)));
        return modelAndView;
    }

    @GetMapping(value = "productList")
    public ModelAndView showProductList(@RequestParam(value = "member") final boolean member){
        ModelAndView modelAndView = new ModelAndView();
        OrderItem orderItem = new OrderItem();
        modelAndView.addObject("orderItem", orderItem);
        if (member) modelAndView.addObject("items", itemService.findAllByItemCategory(Item.category.MEMBER));
        else modelAndView.addObject("items", itemService.findAllByItemCategory(Item.category.STANDARD_USER));
        modelAndView.setViewName("productList");
        return modelAndView;
    }

    @GetMapping(value = "getOne")
    @ResponseBody
    public Item getOne(long id){
        return itemService.findById(id);
    }

    @PostMapping(value = "addToCart")
    public ModelAndView addToCart(OrderItem orderItem){
        personService.saveUser(orderItem.getPerson());

        orderItem.setItem(itemService.findById(orderItem.getItem().getId()));
        ModelAndView modelAndView = new ModelAndView();
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
//        System.out.println("Current Session ID ----------------> " + currentSessionId);
        Cart cartExists = cartService.findBySessionId(currentSessionId);
        if (cartExists != null){
            cartService.deleteCart(cartExists);
            cartExists.getOrderedItems().add(orderItem);
            cartExists.setSumPrice(cartExists.getSumPrice().add(orderItem.getPrice()));
            cartService.saveCart(cartExists);
            modelAndView.addObject("cart", cartExists);
        } else {
            Cart newCart = new Cart();
            newCart.setSessionId(currentSessionId);
            newCart.getOrderedItems().add(orderItem);
            newCart.setSumPrice(orderItem.getPrice());
            cartService.saveCart(newCart);
            modelAndView.addObject("cart", newCart);
        }
        Order newOrder = new Order();
        newOrder.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        modelAndView.addObject("order", newOrder);

        modelAndView.setViewName("cart");
        return modelAndView;
    }
}
