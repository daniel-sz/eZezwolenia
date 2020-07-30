package org.szewczyk.pwr.pzwmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.szewczyk.pwr.pzwmanager.model.Order;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByDate(final String date);
    List<Order> findAllByEmail(final String email);
    Order findFirstByOrderNumber(final String orderNum);
}
