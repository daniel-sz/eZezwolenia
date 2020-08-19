package org.szewczyk.pwr.pzwmanager.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "people")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long id;

    @Column(name = "first_name")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    @Size(min = 2, max = 32, message = "Imię musi mieć długość od 2 do 32 znaków")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "Proszę uzupełnić wszystkie pola")
    @Size(min = 2, max = 64, message = "Nazwisko musi mieć długość od 2 do 64 znaków")
    private String lastName;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "club_name")
    private String clubName;

    @Override
    public String toString() {
        return "(" + firstName + " " + lastName +
                ", Nr karty: " + cardNumber +
                ", Koło PZW: " + clubName + ")";
    }
}
