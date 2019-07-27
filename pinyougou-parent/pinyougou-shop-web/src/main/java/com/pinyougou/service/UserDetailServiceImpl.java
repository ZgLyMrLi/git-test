package com.pinyougou.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    //s是用户在登录页面输入的登录名
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //用户名不对，则返回null
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));//构建一个角色列表

        TbSeller seller = sellerService.findOne(username);
        if (seller != null) {
            if ("1".equals(seller.getStatus())) {
                return new User(username, seller.getPassword(), grantAuths);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
