package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private long id;

    @Column(name = "session_id")
    private String sessionId;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "order_items_cart", joinColumns = @JoinColumn(name = "cart_id"), inverseJoinColumns = @JoinColumn(name = "order_item_id"))
    private List<OrderItem> orderedItems = new ArrayList<>();

    @Column(name = "sum_price")
    private BigDecimal sumPrice;

    public Cart(){
        sumPrice = BigDecimal.ZERO;
    }
}
