package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int id;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "value")
    private BigDecimal value;

}
