package org.szewczyk.pwr.pzwmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.szewczyk.pwr.pzwmanager.model.Person;

import java.util.Optional;

@Repository()
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findById(final Long userId);
    Person findByLastName(final String lastName);
    Person findByCardNumber(final String card);

}
