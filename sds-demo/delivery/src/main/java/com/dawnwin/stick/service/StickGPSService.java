package com.dawnwin.stick.service;

import com.baomidou.mybatisplus.service.IService;
import com.dawnwin.stick.model.StickGPS;
import com.dawnwin.stick.model.StickUser;

public interface StickGPSService extends IService<StickGPS> {
    StickGPS getLatestGPS(int deviceId);
}
