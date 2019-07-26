package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickDeviceMapper;
import com.dawnwin.stick.model.*;
import com.dawnwin.stick.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class StickDeviceServiceImpl extends ServiceImpl<StickDeviceMapper, StickDevice> implements StickDeviceService {
    @Autowired
    private StickDeviceMapper stickDeviceMapper;
    @Autowired
    private StickUserDeviceService userDeviceService;
    @Autowired
    private StickHeartBloodService heartBloodService;
    @Autowired
    private StickWarnService warnService;
    @Autowired
    private StickGPSService gpsService;
    @Autowired
    private StickFenceService fenceService;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public StickDevice findDeviceByImei(String imei) {
        return stickDeviceMapper.getDeviceByImei(imei);
    }

    @Override
    public List<StickDevice> listDeviceByUserId(int userId) {
        return userDeviceService.listDevicesByUserId(userId);
    }

    @Override
    public void removeUserDevice(int userId, String imei){
        StickDevice dev = findDeviceByImei(imei);
        if(dev == null){
            return;
        }
        StickUserDevice cond = new StickUserDevice();
        cond.setUserId(userId);
        List<StickUserDevice> existRelas = userDeviceService.selectList(new EntityWrapper<>(cond));
        if(existRelas!=null && existRelas.size()>0){
            boolean isDefaultDeleted = false;
            boolean isNewDefaultSet = false;
            for(StickUserDevice userDevice:existRelas) {
                if (userDevice.getDeviceId() == dev.getDeviceId()) {
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
        dev.reset();
        updateById(dev);
        StickHeartBlood hb = new StickHeartBlood();
        hb.setDeviceId(dev.getDeviceId());
        heartBloodService.delete(new EntityWrapper<>(hb));
        StickWarn w = new StickWarn();
        w.setDeviceId(dev.getDeviceId());
        warnService.delete(new EntityWrapper<>(w));
        StickGPS gps = new StickGPS();
        gps.setDeviceId(dev.getDeviceId());
        gpsService.delete(new EntityWrapper<>(gps));
        StickFence fc = new StickFence();
        fc.setDeviceId(dev.getDeviceId());
        List<StickFence> fences = fenceService.selectList(new EntityWrapper<>(fc));
        try {
            for (StickFence fence : fences) {
                restTemplate.delete("https://restapi.amap.com/v4/geofence/meta?key=178d7cef1209656b6d17dda618778330&gid=" + fence.getAmapGid());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        fenceService.delete(new EntityWrapper<>(fc));

    }

    @Override
    public boolean updateById(StickDevice entity) {
        return stickDeviceMapper.updateById(entity)>0;
    }
}
