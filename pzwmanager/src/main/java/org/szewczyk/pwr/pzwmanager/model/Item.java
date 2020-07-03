package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long id;

    @Column(name = "item_name")
    @NotEmpty(message = "*Nazwa nie może być pusta!")
    private String name;

    @Column(name = "item_descripiton")
    private String description;

    @Column(name = "item_price")
    @NotEmpty(message = "*Cena nie może być pusta!")
    private BigDecimal price;
}
