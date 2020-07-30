package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Order;
import org.szewczyk.pwr.pzwmanager.repository.OrderRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository){ this.orderRepository = orderRepository; }

    public Order findByOrderNum(String num){ return orderRepository.findFirstByOrderNumber(num); }

    public List<Order> findAllByDate(String date){ return orderRepository.findAllByDate(date); }
    public List<Order> findAllByEmail(String email){ return  orderRepository.findAllByEmail(email); }
    public Order saveOrder(Order order){
        orderRepository.save(order);
        return order;
    }
    public void deleteById(long id){ orderRepository.deleteById(id); }
    public List<Order> findAll(){ return orderRepository.findAll(); }

}
