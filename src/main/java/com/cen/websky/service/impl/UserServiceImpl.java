package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.pojo.po.User;
import com.cen.websky.mapper.UserMapper;
import com.cen.websky.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public List<User> list() {
        List<User> users = lambdaQuery().list();
        return users;
    }

    @Override
    public User login(User user) {
        user = lambdaQuery()
                .select(User::getId, User::getUsername, User::getPassword, User::getEmail, User::getImage)
                .eq(User::getEmail, user.getEmail())
                .eq(User::getPassword, user.getPassword())
                .one();
        return user;
    }
}
