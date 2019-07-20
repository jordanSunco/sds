package com.codingapi.sds.socket.mq;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * create by lorne on 2017/12/6
 */
@FeignClient(value = "delivery")
@Component
public interface DeliveryClient {

    @RequestMapping(value = "/online/add",method = RequestMethod.POST)
    boolean add(@RequestParam("modelName") String modelName,@RequestParam("uniqueKey") String uniqueKey);


    @RequestMapping(value = "/online/remove",method = RequestMethod.POST)
    boolean remove(@RequestParam("modelName") String modelName,@RequestParam("uniqueKey") String uniqueKey);


    @RequestMapping(value = "/online/putKey",method = RequestMethod.POST)
    boolean putKey(@RequestParam("modelName") String modelName,@RequestParam("uniqueKey") String uniqueKey,@RequestParam("key") String key);

    @RequestMapping(value = "/delivery/sendStrCmdByKey",method =  RequestMethod.POST)
    boolean sendStrCmdByKey(@RequestParam(name = "key") String key, @RequestParam(name = "cmd") String cmd);

    @GetMapping(value = "/stick/check")
    boolean check(@RequestParam(name = "imei") String imei);

    @RequestMapping(value = "/stick/rec",method = RequestMethod.POST)
    boolean receive(@RequestParam(name = "cmd") String cmd,
                    @RequestParam(name = "imei") String imei,
                    @RequestParam(name = "power") String power,
                    @RequestParam(name = "data") String data);

}
