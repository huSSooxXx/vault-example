package com.example.vaultexample.business.controller;

import com.example.vaultexample.business.entity.Authorites;
import com.example.vaultexample.business.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("user")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/list")
    public List<Authorites> getInfo(){
        return userRepository.findAll();
    }
}
