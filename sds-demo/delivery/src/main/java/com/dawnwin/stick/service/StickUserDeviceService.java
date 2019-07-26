package com.dawnwin.stick.service;

import com.baomidou.mybatisplus.service.IService;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUserDevice;

import java.util.List;

public interface StickUserDeviceService extends IService<StickUserDevice> {
    List<StickDevice> listDevicesByUserId(int userId);
    void deleteByUserAndDevice(int userId, int deviceId);
}
