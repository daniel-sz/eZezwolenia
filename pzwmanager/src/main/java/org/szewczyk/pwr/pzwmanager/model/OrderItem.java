package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    public enum periods {
        DAY1,
        DAYS3,
        DAYS7,
        DAYS365
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private long id;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Item item;

    @Column(name = "selected_period")
    @NotEmpty(message = "Pole wymagane")
    private periods selectedPeriod;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "begin_date")
    @NotBlank(message = "Pole wymagane")
    private String beginDate;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Person person;

    @Override
    public String toString() {
        String period = "";
        if (price.equals(item.getPrice1day())) period = "1 dzień";
        if (price.equals(item.getPrice3days())) period = "3 dni";
        if (price.equals(item.getPrice7days())) period = "7 dni";
        if (price.equals(item.getPrice1year())) period = "1 rok";
        return item.getName() + " [" + period + " od: " + beginDate + "] - " + person.toString();
    }
}
