package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.OrderItem;
import org.szewczyk.pwr.pzwmanager.repository.OrderItemRepository;

import java.util.List;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository){ this.orderItemRepository = orderItemRepository; }


    public OrderItem saveOrderItem(OrderItem orderItem){
        orderItemRepository.save(orderItem);
        return orderItem;
    }
}
