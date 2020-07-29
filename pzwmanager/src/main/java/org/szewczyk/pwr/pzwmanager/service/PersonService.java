package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Person;
import org.szewczyk.pwr.pzwmanager.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private List<Person> people;

    @Autowired
    public PersonService(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    public Person findById(Long id){
        return personRepository.findById(id).orElseThrow();
    }
    public Person findByLastName(String lastName){ return personRepository.findByLastName(lastName); }
    public Person findByCardNumber(String cardNumber){ return personRepository.findByCardNumber(cardNumber); }
    public Person saveUser(Person person){
        personRepository.save(person);
        return person;
    }
}
