package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    @NotEmpty
    @Column(name = "firstName")
    private String firstName;

    @NotEmpty
    @Column(name = "lastName")
    private String lastName;

//    @NotEmpty
    @Column(name = "address")
    private String address;

//    @NotEmpty
    @Column(name = "card_id")
    private String cardId;

//    @NotEmpty
    @Column(name = "age")
    private int age;

//    @NotEmpty
    @Column(name = "email")
    private String email;

}
