package com.dawnwin.stick.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUser;
import com.dawnwin.stick.model.StickUserDevice;

import java.util.List;
import java.util.Map;

public interface StickUserDeviceMapper extends BaseMapper<StickUserDevice> {
    List<StickDevice> listDevicesByUserId(int userId);
    void deleteByUserIdAndDeviceId(Map<String,Object> params);
}
