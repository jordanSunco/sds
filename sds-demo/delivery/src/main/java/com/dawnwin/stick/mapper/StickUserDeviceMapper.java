package com.dawnwin.stick.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUser;
import com.dawnwin.stick.model.StickUserDevice;

import java.util.List;

public interface StickUserDeviceMapper extends BaseMapper<StickUserDevice> {
    List<StickDevice> listLoveDeviceByUserId(int userId);
}
