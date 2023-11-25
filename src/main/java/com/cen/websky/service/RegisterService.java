package com.cen.websky.service;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;

public interface RegisterService {
    Result register(User user);
}
