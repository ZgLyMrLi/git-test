package com.itlyf.demo.service.com.itlyf.demo.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @RequestMapping("/findLoginUser")
    public void findLoginUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录名:"+username);
    }
}
