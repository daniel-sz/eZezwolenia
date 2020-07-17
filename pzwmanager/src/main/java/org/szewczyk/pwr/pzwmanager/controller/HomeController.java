package org.szewczyk.pwr.pzwmanager.controller;

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
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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

    @GetMapping(value = "/")
    public ModelAndView hello(){
        ModelAndView modelAndView = new ModelAndView("main");
        Cart newCart = new Cart();
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        System.out.println("Current Session ID ----------------> " + currentSessionId);
        if (cartService.findBySessionId(currentSessionId) == null){
            newCart.setSessionId(currentSessionId);
            cartService.saveCart(newCart);
        }
//        modelAndView.addObject("cart", newCart);
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

//    @GetMapping(value = "oplaty")
//    public ModelAndView mainWindow(){
//        return new ModelAndView("main");
//    }

//    @GetMapping(value = "dodaj")
//    public ModelAndView addUser(){
//        ModelAndView modelAndView = new ModelAndView();
//        Person person = new Person();
//        modelAndView.addObject("user", person);
//        modelAndView.setViewName("newClient");
//        return modelAndView;
//    }
//
//    @PostMapping(value = "dodaj")
//    public ModelAndView addNewUser(@Valid Person person, BindingResult bindingResult){
//        ModelAndView modelAndView = new ModelAndView();
//        Person personExists = personService.findByCardNumber(person.getCardNumber());
//        if (personExists != null){
//            bindingResult.rejectValue("cardId", "error.user", "Użytkownik z takim numerem karty już istnieje");
//        }
//        if (bindingResult.hasErrors()){
//            modelAndView.setViewName("newClient");
//        } else {
//            personService.saveUser(person);
//            modelAndView.addObject("successMessage", "Pomyślnie dodano użytkownika");
//            modelAndView.addObject("person", new Person());
//            modelAndView.setViewName("newClient");
//        }
//        return modelAndView;
//    }

    @GetMapping(value = "addItem")
    public ModelAndView addItemGet(){
        ModelAndView modelAndView = new ModelAndView();
        Item item = new Item();
        modelAndView.addObject("item", item);
        modelAndView.setViewName("addItem");
        return modelAndView;
    }

    @PostMapping(value = "addItem")
    public ModelAndView addItemPost(@Valid Item item, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView();
        Item itemExists = itemService.findByName(item.getName());
        if (itemExists != null){
            bindingResult.rejectValue("name", "error.item", "Istnieje już pozycja o takiej nazwie");
        }
        if (bindingResult.hasErrors()){
            modelAndView.setViewName("addItem");
        } else {
            itemService.saveItem(item);
            modelAndView.addObject("successMessage", "Pomyślnie dodano do bazy");
            modelAndView.addObject("item", new Item());
            modelAndView.setViewName("addItem");
        }
        return modelAndView;
    }

    @GetMapping(value = "showItems")
    public ModelAndView showItemsList(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("items", itemService.findAll());
        modelAndView.setViewName("showItems");
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
        System.out.println("Current Session ID ----------------> " + currentSessionId);
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
        newOrder.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        modelAndView.addObject("order", newOrder);

        modelAndView.setViewName("cart");
        return modelAndView;
    }

    @RequestMapping(value = "finalizeOrder")
    public ModelAndView finalizeOrder(Order order){
        ModelAndView modelAndView = new ModelAndView();
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        Cart cart = cartService.findBySessionId(currentSessionId);
        Order placedOrder = new Order();

        order.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        order.setOrderNumber();
        order.getOrderItems().addAll(cart.getOrderedItems());
        cart.getOrderedItems().clear();
//        cartService.deleteCart(cart);
        order.setValue(cart.getSumPrice());
        orderService.saveOrder(order);

        System.out.println("Zamówienie nr: " + order.getOrder_number() + " o wartości: " + order.getValue()
                + " na adres: " + order.getEmail());
        modelAndView.addObject("orderDetails", order);
        modelAndView.setViewName("finalizeOrder");
        return modelAndView;
    }

    @GetMapping(value = "/cart")
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
}
