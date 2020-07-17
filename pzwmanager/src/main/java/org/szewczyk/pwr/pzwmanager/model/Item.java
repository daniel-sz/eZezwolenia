package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "items")
public class Item {

    public enum category{
        MEMBER,
        STANDARD_USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long id;

    @Column(name = "item_name")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private String name;

    @Column(name = "item_descripiton")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private String description;

    @Column(name = "item_category")
    @NotEmpty(message = "Proszę uzupełnić wszystkie pola")
    private category itemCategory;

    @Column(name = "price_1")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private BigDecimal price1day;

    @Column(name = "price_3")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private BigDecimal price3days;

    @Column(name = "price_7")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private BigDecimal price7days;

    @Column(name = "price365")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    private BigDecimal price1year;

    public Item(){}
    public Item(String name, String description, category cat, BigDecimal price1day, BigDecimal price3days, BigDecimal price7days, BigDecimal price1year){
        this.name = name;
        this.description = description;
        this.itemCategory = cat;
        this.price1day = price1day;
        this.price3days = price3days;
        this.price7days = price7days;
        this.price1year = price1year;
    }
}
