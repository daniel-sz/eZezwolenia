package org.szewczyk.pwr.pzwmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.Item;
import org.szewczyk.pwr.pzwmanager.model.User;
import org.szewczyk.pwr.pzwmanager.service.ItemService;
import org.szewczyk.pwr.pzwmanager.service.UserService;

import javax.annotation.Resource;
import javax.validation.Valid;

@Controller
public class HomeController {
    @Resource
    private UserService userService;
    @Resource
    private ItemService itemService;

    @GetMapping(value = "/")
    public ModelAndView hello(){
        return new ModelAndView("hello");
    }

    @GetMapping(value = "oplaty")
    public ModelAndView mainWindow(){
        return new ModelAndView("main");
    }

    @GetMapping(value = "dodaj")
    public ModelAndView addUser(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("newClient");
        return modelAndView;
    }

    @PostMapping(value = "dodaj")
    public ModelAndView addNewUser(@Valid User user, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findByCardId(user.getCardId());
        if (userExists != null){
            bindingResult.rejectValue("cardId", "error.user", "Użytkownik z takim numerem karty już istnieje");
        }
        if (bindingResult.hasErrors()){
            modelAndView.setViewName("newClient");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "Pomyślnie dodano użytkownika");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("newClient");
        }
        return modelAndView;
    }

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
}
