package com.itlyf.demo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itlyf.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    public String getName() {
        return "itlyf";
    }

}
