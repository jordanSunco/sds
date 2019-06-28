package com.codingapi.sds.delivery.service.impl;

import com.codingapi.sds.delivery.service.RedisService;
import com.codingapi.sds.delivery.service.SocketService;
import com.codingapi.sds.delivery.service.StickService;
import com.lorne.core.framework.exception.ServiceException;
import com.lorne.core.framework.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StickServiceImpl implements StickService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SocketService socketService;

    @Override
    public boolean startLocation(String imei) {
        try {
            String cmd = String.format("{%s#WEBLOCATION}\r\n",imei);
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setSosList(String imei, String phoneNumberList) {
        try {
            String cmd = String.format("{%s#SOSLIST#%s#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"), phoneNumberList);
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean startMonitor(String imei, String mobile) {
        try {
            String cmd = String.format("{%s#MONITOR#%s#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"),mobile);
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean shutdownStick(String imei) {
        try {
            String cmd = String.format("{%s#SHUTDOWN#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean resetStick(String imei) {
        try {
            String cmd = String.format("{%s#RESET#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean openGPS(String imei) {
        try {
            String cmd = String.format("{%s#OPENGPS#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean closeGPS(String imei) {
        try {
            String cmd = String.format("{%s#CLOSEGPS#%s}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"));
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setInterval(String imei, int interval) {
        try {
            String cmd = String.format("{%s#INTERVAL#%s#%d}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"),interval);
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setWifiInterval(String imei, int interval) {
        try {
            String cmd = String.format("{%s#WIFIINTERVAL#%s#%d}\r\n",imei, DateUtil.formatDate(new Date(),"YYYYMMddHHmmss"),interval);
            return socketService.sendStrCmdByKey(imei, cmd);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        }
    }
}
