package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.User;
import org.szewczyk.pwr.pzwmanager.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;
    private List<User> users;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User findById(int id){ return userRepository.findById(id); }
    public User findByLastName(String lastName){ return userRepository.findByLastName(lastName); }
    public User findByCardId(String cardId){ return userRepository.findByCardId(cardId); }
    public User saveUser(User user){
        userRepository.save(user);
        return user;
    }
}
