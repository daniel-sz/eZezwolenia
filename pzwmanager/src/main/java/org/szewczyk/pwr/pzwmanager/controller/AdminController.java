package org.szewczyk.pwr.pzwmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.szewczyk.pwr.pzwmanager.model.Item;
import org.szewczyk.pwr.pzwmanager.service.ItemService;
import org.szewczyk.pwr.pzwmanager.service.OrderService;

import javax.annotation.Resource;
import javax.validation.Valid;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    @Resource
    private ItemService itemService;
    @Resource
    private OrderService orderService;

    @GetMapping(value = "")
    public ModelAndView adminHome(){
        ModelAndView modelAndView = new ModelAndView("admin/adminPanel");
        modelAndView.addObject("items", itemService.findAll());
        modelAndView.addObject("orders", orderService.findAll());
        return modelAndView;
    }

    @GetMapping(value = "showItems")
    public ModelAndView showItemsList(){
        ModelAndView modelAndView = new ModelAndView("admin/showItems");
        modelAndView.addObject("items", itemService.findAll());
        return modelAndView;
    }

    @GetMapping(value = "addItem")
    public ModelAndView addItemGet(){
        ModelAndView modelAndView = new ModelAndView();
        Item item = new Item();
        modelAndView.addObject("item", item);
        modelAndView.setViewName("admin/addItem");
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
            modelAndView.setViewName("admin/addItem");
        } else {
            itemService.saveItem(item);
            modelAndView.addObject("successMessage", "Pomyślnie dodano do bazy");
            modelAndView.addObject("item", new Item());
            modelAndView.setViewName("admin/addItem");
        }
        return modelAndView;
    }

    @PostMapping(value = "/removeItem")
    public String removeItem(@RequestParam(value = "id") final long id){
        itemService.remove(id);
        return "redirect:";
    }
}
