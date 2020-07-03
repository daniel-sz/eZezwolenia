package org.szewczyk.pwr.pzwmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.szewczyk.pwr.pzwmanager.model.User;

@Repository()
public interface UserRepository extends JpaRepository<User, Long> {
    User findById(final int id);
    User findByLastName(final String lastName);
    User findByCardId(final String card);
}
