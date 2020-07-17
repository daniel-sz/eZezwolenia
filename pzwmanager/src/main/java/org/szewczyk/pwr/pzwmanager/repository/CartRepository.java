package org.szewczyk.pwr.pzwmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.szewczyk.pwr.pzwmanager.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findById(final long id);
    Cart findBySessionId(final String sessionId);
    void deleteCartBySessionId(final String sessionId);
    void removeCartBySessionId(final String sessionId);
}
