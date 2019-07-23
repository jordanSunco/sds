package com.dawnwin.app.stick.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.codingapi.sds.socket.mq.DeliveryClient;
import com.codingapi.sds.socket.service.SocketControl;
import com.codingapi.sds.socket.utils.SocketManager;
import com.dawnwin.app.stick.model.StickCommand;
import com.dawnwin.app.stick.service.StickService;
import com.lorne.core.framework.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StickServiceImpl implements StickService {

    @Autowired
    private DeliveryClient deliveryClient;

    @Override
    public boolean checkDevice(String imei) {
        return deliveryClient.check(imei);
    }

    @Override
    public String processStickCommand(String modelName, String uniqueKey, StickCommand command) {
        String cmdType = command.getCmd();
        String result = "";
        if(StickCommand.DEVICE_LOGIN.equals(cmdType)){
            deliveryClient.putKey(modelName, uniqueKey,command.getDeviceImei());
            Date now = new Date();
            result = String.format("{%s#LOGIN_OK#%s#%s}", command.getDeviceImei(),DateUtil.formatDate(now,"YYYYMMdd"),DateUtil.formatDate(now,"HHmmss"));
        }
        if(StickCommand.DEVICE_HEARTBEAT.equals(cmdType)){
            result = String.format("{%s#HEARTBEAT_OK}",command.getDeviceImei());
        }
        if(StickCommand.DEVICE_FALLDOWN.equals(cmdType)){
            result = String.format("{%s#FALLDOWN_OK}",command.getDeviceImei());
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(),"跌倒警告");
        }
        if(StickCommand.DEVICE_BLOODPRESS.equals(cmdType)){
            result = String.format("{%s#BLOODPRESS_OK#%s}",command.getDeviceImei(), DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(), command.getCmdData());
        }
        if(StickCommand.DEVICE_GPS_LBS_WIFI.equals(cmdType)){
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(), command.getCmdData());
            result = String.format("{%s#LBS_WIFI_OK}",command.getDeviceImei());
        }
        if(StickCommand.DEVICE_GPS_LBS.equals(cmdType)){
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(), command.getCmdData());
            result = String.format("{%s#LBS_OK}",command.getDeviceImei());
        }
        if(StickCommand.DEVICE_GPS_WIFI.equals(cmdType)){
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(), command.getCmdData());
            result = String.format("{%s#WIFI_OK}",command.getDeviceImei());
        }
        if(StickCommand.DEVICE_GPS.equals(cmdType)){
            result = String.format("{%s#GPS_OK}",command.getDeviceImei());
            //解析GPS数据，发给数据库
            /*JSONObject obj = new JSONObject();
            String cmdData = command.getCmdData();
            String[] datas = cmdData.split("|");
            if(datas.length >= 2){
                String lat = datas[0];
                String latDirection = lat.split("-")[0];
                double latitude = Double.parseDouble(lat.split("-")[1]);
                String lot = datas[1];
                String longDirection = lot.split("-")[0];
                double longitude = Double.parseDouble(lot.split("-")[1]);
                obj.put("latitude", latitude);
                obj.put("latDirection", latDirection);
                obj.put("longitude", longitude);
                obj.put("longDirection", longDirection);
                if(datas.length>2) {
                    obj.put("direction", datas[2]);
                }
                if(datas.length>3) {
                    obj.put("speed", datas[3]);
                }
                if(datas.length>4) {
                    obj.put("satellite", datas[4]);
                }
                if(datas.length>5) {
                    obj.put("signal", datas[5]);
                }
            }*/
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getPower(), command.getCmdData());
        }
        return result;
    }

    @Override
    public void sendCommand(String imsi, String cmd) {
        deliveryClient.sendStrCmdByKey(imsi, cmd+"\r\n");
    }
}
