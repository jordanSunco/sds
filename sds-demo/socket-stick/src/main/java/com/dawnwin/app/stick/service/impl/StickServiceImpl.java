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
            deliveryClient.receive(cmdType, command.getDeviceImei(),command.getCmdData());
        }
        if(StickCommand.DEVICE_BLOODPRESS.equals(cmdType)){
            result = String.format("{%s#BLOODPRESS_OK#%s}",command.getDeviceImei(), DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            deliveryClient.receive(cmdType, command.getDeviceImei(), command.getCmdData());
        }
        if(StickCommand.DEVICE_LBS_WIFI.equals(cmdType)){
            //TODO 设备上报了GPS，需要记录到数据库
            //result = String.format("{%s#HEARTBEAT_OK}",command.getDeviceImei());
        }
        if(StickCommand.DEVICE_GPS.equals(cmdType)){
            result = String.format("{%s#GPS_OK}",command.getDeviceImei());
            String cmdData = command.getCmdData();
            //TODO 如何解析GPS数据？
            JSONObject obj = new JSONObject();

            deliveryClient.receive(cmdType, command.getDeviceImei(), obj.toJSONString());
        }
        return result;
    }

    @Override
    public void sendCommand(String imsi, String cmd) {
        deliveryClient.sendStrCmdByKey(imsi, cmd+"\r\n");
    }
}
