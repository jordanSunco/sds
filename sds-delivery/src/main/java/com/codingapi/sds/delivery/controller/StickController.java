package com.codingapi.sds.delivery.controller;

import com.codingapi.sds.delivery.service.StickService;
import com.lorne.core.framework.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * create by jordan on 2017/10/10
 */
@RestController
@RequestMapping("/stick")
public class StickController {


    @Autowired
    private StickService stickService;

    @RequestMapping(value = "/index")
    public boolean index(){
        return true;
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public boolean send(@RequestParam(name = "imei") String imei,
                        @RequestParam(name = "cmd") String cmd,
                        @RequestParam(name = "data") String data) throws ServiceException {
        if(StringUtils.isEmpty(imei) || StringUtils.isEmpty("cmd")){
            return false;
        }
        if("SOSLIST".equals(cmd)){
            return stickService.setSosList(imei, data);
        }
        if("MONITOR".equals(cmd)){
            return stickService.startMonitor(imei, data);
        }
        if("WEBLOCATION".equals(cmd)){
            return stickService.startLocation(imei);
        }
        if("SHUTDOWN".equals(cmd)){
            return stickService.shutdownStick(imei);
        }
        if("RESET".equals(cmd)){
            return stickService.resetStick(imei);
        }
        if("OPENGPS".equals(cmd)){
            return stickService.openGPS(imei);
        }
        if("CLOSEGPS".equals(cmd)){
            return stickService.closeGPS(imei);
        }
        if("INTERVAL".equals(cmd)){
            return stickService.setInterval(imei, Integer.parseInt(data));
        }
        if("WIFIINTERVAL".equals(cmd)){
            return stickService.setWifiInterval(imei, Integer.parseInt(data));
        }
        return true;
    }

}
