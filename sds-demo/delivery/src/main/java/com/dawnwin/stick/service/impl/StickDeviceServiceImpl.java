package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickDeviceMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUserDevice;
import com.dawnwin.stick.service.StickDeviceService;
import com.dawnwin.stick.service.StickUserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StickDeviceServiceImpl extends ServiceImpl<StickDeviceMapper, StickDevice> implements StickDeviceService {
    @Autowired
    private StickDeviceMapper stickDeviceMapper;
    @Autowired
    private StickUserDeviceService userDeviceService;

    @Override
    public StickDevice findDeviceByImei(String imei) {
        return stickDeviceMapper.getDeviceByImei(imei);
    }

    @Override
    public List<StickDevice> listDeviceByUserId(int userId) {
        return userDeviceService.listDevicesByUserId(userId);
    }

    @Override
    public void removeUserDevice(int userId, int devideId){
        StickUserDevice cond = new StickUserDevice();
        cond.setUserId(userId);
        List<StickUserDevice> existRelas = userDeviceService.selectList(new EntityWrapper<>(cond));
        if(existRelas!=null && existRelas.size()>0){
            boolean isDefaultDeleted = false;
            boolean isNewDefaultSet = false;
            for(StickUserDevice userDevice:existRelas) {
                if (userDevice.getDeviceId() == devideId) {
                    //如果是默认设备，则把该账号下其他设备设置为默认设备
                    userDevice.deleteById();
                    if(userDevice.getUserDefault()) {
                        isDefaultDeleted = true;
                    }
                }else{
                    //不是默认设备
                    if(isDefaultDeleted && !isNewDefaultSet){
                        userDevice.setUserDefault(true);
                        userDevice.updateById();
                        isNewDefaultSet = true;
                    }
                }
            }
        }
        selectById(devideId).reset();
    }

    @Override
    public boolean updateById(StickDevice entity) {
        super.updateById(entity);
        return stickDeviceMapper.updateById(entity)>0;
    }
}
