package com.dawnwin.stick.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.dawnwin.stick.model.StickDevice;

public interface StickDeviceMapper extends BaseMapper<StickDevice> {
    StickDevice getDeviceByImei(String imei);
    Integer updateById(StickDevice device);
}
