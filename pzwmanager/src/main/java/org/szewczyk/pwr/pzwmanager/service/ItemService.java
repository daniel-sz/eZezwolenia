package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Item;
import org.szewczyk.pwr.pzwmanager.repository.ItemRepository;

import java.util.List;

@Service
public class ItemService {
    private ItemRepository itemRepository;
    private List<Item> items;

    @Autowired
    public ItemService(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    public Item findByName(String name){ return itemRepository.findByName(name); }
    public List<Item> findAll(){ return itemRepository.findAll(); }
    public Item saveItem(Item item){
        itemRepository.save(item);
        return item;
    }
}
