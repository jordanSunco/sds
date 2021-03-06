package com.dawnwin.stick.service;

import com.baomidou.mybatisplus.service.IService;
import com.dawnwin.stick.model.StickDevice;

import java.util.List;
import java.util.Map;

public interface StickDeviceService extends IService<StickDevice> {
    StickDevice findDeviceByImei(String imei);
    List<StickDevice> listDeviceByUserId(int userId);
    void removeUserDevice(int userId, String imei);
}
