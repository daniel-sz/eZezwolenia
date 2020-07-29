package org.szewczyk.pwr.pzwmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Item;
import org.szewczyk.pwr.pzwmanager.repository.ItemRepository;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    public Item findByName(String name){ return itemRepository.findByName(name); }
    public List<Item> findAll(){ return itemRepository.findAll(); }
    public List<Item> findAllByItemCategory(Item.category category){
        return itemRepository.findAllByItemCategory(category);
    }
    public Item findById(long id){ return itemRepository.findById(id).orElseThrow(); }
    public Item saveItem(Item item){
        itemRepository.save(item);
        return item;
    }
    public void remove(long id){ itemRepository.deleteById(id); }
}
