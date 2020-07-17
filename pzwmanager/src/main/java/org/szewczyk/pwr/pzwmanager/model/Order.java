package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long id;

    @Column(name = "date")
    private String date;

    @Column(name = "order_number")
    private String order_number;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "email")
    @Email(message = "Adres e-mail musi być prawidłowy")
    private String email;

    @OneToMany
    @JoinTable(name = "order_all_items", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "order_item_id"))
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(){}
    public void setOrderNumber(){ this.order_number = "PZW/Kud/" + this.id + "/" + this.date.substring(0, 4); }
}
