package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Cart;
import org.szewczyk.pwr.pzwmanager.repository.CartRepository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
public class CartService {
    CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository){ this.cartRepository = cartRepository; }

    public Cart findById(long id){
        return cartRepository.findById(id);
    }
    public Cart findBySessionId(String sessionId){ return cartRepository.findBySessionId(sessionId); }

    @Transactional
    public Cart saveCart(Cart cart){
        cartRepository.save(cart);
        cartRepository.flush();
        return cart;
    }

    @Transactional
    public Cart deleteCart(Cart cart){
//        System.out.println("\\____________DELETING CART____________/");
        cartRepository.removeCartBySessionId(cart.getSessionId());
        cartRepository.flush();
        return cart;
    }
}
