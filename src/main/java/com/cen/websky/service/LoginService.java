package com.cen.websky.service;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;

public interface LoginService {
    Result login(User user);
}
