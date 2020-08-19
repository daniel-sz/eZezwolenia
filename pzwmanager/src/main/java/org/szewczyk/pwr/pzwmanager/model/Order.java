package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    public enum Status{
        PENDING,
        ERROR,
        SUCCESS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long id;

    @Column(name = "date")
    private String date;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "email")
    @Email(message = "Adres e-mail musi być prawidłowy")
    private String email;

    @OneToMany
    @JoinTable(name = "order_all_items", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "order_item_id"))
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "status")
    private Status status;

    @Column(name = "payu_order_id")
    private String payuOrderId;

    @Column(name = "payu_payment_id")
    private String payuPaymentId;

    public Order(){}
    public void setOrderNumber(){
        String orderNum = "PZW/Kud/" + this.date.substring(0,7);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(this.date.getBytes());
            byte[] digest = md.digest();
            orderNum = orderNum.concat("/" + DatatypeConverter.printHexBinary(digest).toUpperCase());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.orderNumber = orderNum;
    }
}
