package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickUserMapper;
import com.dawnwin.stick.model.StickUser;
import com.dawnwin.stick.service.StickUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StickUserServiceImpl extends ServiceImpl<StickUserMapper, StickUser> implements StickUserService {
    @Autowired
    private StickUserMapper stickUserMapper;

    @Override
    public StickUser login(String mobile, String pwd) {
        StickUser cond = new StickUser();
        cond.setMobile(mobile);
        cond.setPassword(pwd);
        return selectOne(new EntityWrapper<>(cond));
    }

    @Override
    public StickUser selectByMobile(String mobile) {
        return selectOne(new EntityWrapper<StickUser>().eq("mobile", mobile));
    }
}
