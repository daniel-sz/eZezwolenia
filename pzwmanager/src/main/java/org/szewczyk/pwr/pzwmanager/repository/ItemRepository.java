package org.szewczyk.pwr.pzwmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.szewczyk.pwr.pzwmanager.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    public Item findByName(final String name);

}
