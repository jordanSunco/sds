package com.dawnwin.stick.service;

import com.baomidou.mybatisplus.service.IService;
import com.dawnwin.stick.model.StickUser;

public interface StickUserService extends IService<StickUser> {
    StickUser login(String mobile, String pwd);
    StickUser selectByMobile(String mobile);
}
