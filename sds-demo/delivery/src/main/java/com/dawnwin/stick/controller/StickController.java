package com.dawnwin.stick.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.dawnwin.stick.config.SMSTemplateConfig;
import com.dawnwin.stick.model.*;
import com.dawnwin.stick.service.*;
import com.dawnwin.stick.utils.JwtHelper;
import com.lorne.core.framework.exception.ServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * create by jordan on 2017/10/10
 */
@RestController
@RequestMapping("/stick")
public class StickController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private StickService stickService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private SMSTemplateConfig smsTemplateConfig;
    @Autowired
    private StickUserService userService;
    @Autowired
    private StickDeviceService deviceService;
    @Autowired
    private StickGPSService gpsService;
    @Autowired
    private StickWarnService warnService;
    @Autowired
    private StickHeartBloodService heartBloodService;
    @Autowired
    private StickFenceService fenceService;

    private String getMobile(){
        String mobile = (String) request.getAttribute("mobile");
        return mobile;
    }

    @RequestMapping(value = "/api/index")
    public boolean index(){
        return true;
    }

    @PostMapping(value = "/api/reg")
    public R<Boolean> reg(@RequestParam(name = "mobile") String mobile,
                          @RequestParam(name = "password") String password,
                          @RequestParam(name = "nickname") String nickname){
        R<Boolean> ret = new R<>(false);
        StickUser user = new StickUser();
        user.setMobile(mobile);
        StickUser existUser = userService.selectByMobile(mobile);
        if (existUser != null && existUser.getUserId() > 0) {
            ret.setCode(1002);
            ret.setMsg("注册失败，手机号码已存在");
        } else {
            user.setNickName(nickname);
            user.setPassword(password);
            user.setAddTime(new Date());
            boolean isOK = userService.insert(user);
            if (isOK) {
                ret.setCode(1000);
                ret.setMsg("注册成功");
            } else {
                ret.setCode(1001);
                ret.setMsg("注册失败");
            }
            ret.setData(isOK);
        }
        return ret;
    }

    @PostMapping(value = "/api/sendcode")
    public R<Boolean> sendCode(@RequestParam(name = "mobile") String mobile){
        R<Boolean> ret = new R<>(true);
        if(!StringUtils.isEmpty(mobile)){
            String code = (String) redisTemplate.opsForValue().get("code" + mobile);
            String randomNumber = RandomUtil.randomNumbers(6);
            // 缓存中不存在code
            if(StringUtils.isEmpty(code)){
                redisTemplate.opsForValue().set("code" + mobile, randomNumber,120, TimeUnit.SECONDS);
                final String accessKeyId = smsTemplateConfig.getAccessKey();
                final String accessKeySecret = smsTemplateConfig.getSecretKey();
                DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
                IAcsClient client = new DefaultAcsClient(profile);

                CommonRequest request = new CommonRequest();
                //request.setProtocol(ProtocolType.HTTPS);
                request.setMethod(MethodType.POST);
                request.setDomain("dysmsapi.aliyuncs.com");
                request.setVersion("2017-05-25");
                request.setAction("SendSms");
                request.putQueryParameter("RegionId", "cn-hangzhou");
                request.putQueryParameter("PhoneNumbers", mobile);
                request.putQueryParameter("SignName", "智能手杖");
                request.putQueryParameter("TemplateCode", smsTemplateConfig.getTemplateCode());
                request.putQueryParameter("TemplateParam", "{\"code\":\""+randomNumber+"\"}");
                try {
                    CommonResponse response = client.getCommonResponse(request);
                    JSONObject res = JSON.parseObject(response.getData());
                    String msg =  res.getString("Message");
                    if("OK".equals(msg)){
                        ret.setCode(1000);
                        ret.setMsg("短信发送成功");
                    }else{
                        ret.setCode(1002);
                        ret.setMsg("短信发送失败");
                    }
                }catch (ServerException e) {
                    e.printStackTrace();
                    ret.setCode(1002);
                    ret.setMsg("短信发送失败");
                } catch (ClientException e) {
                    e.printStackTrace();
                    ret.setCode(1002);
                    ret.setMsg("短信发送失败");
                }
            }else{
                ret.setCode(1003);
                ret.setMsg("请勿频繁发送");
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/resetPass")
    public R<Boolean> resetPass(@RequestParam(name = "mobile") String mobile,
                                @RequestParam(name = "checkcode") String checkcode,
                                @RequestParam(name = "newpassword") String newpassword){
        R<Boolean> ret = new R<>(false);
        if(!StringUtils.isEmpty(mobile) && !StringUtils.isEmpty(checkcode) && !StringUtils.isEmpty(newpassword)){
            String code = redisTemplate.opsForValue().get("code" + mobile);
            if(!StringUtils.isEmpty(code) && code.equals(checkcode)){
                StickUser user = userService.selectByMobile(mobile);
                if(user == null){
                    ret.setCode(1003);
                    ret.setMsg("用户不存在");
                }else{
                    user.setPassword(newpassword);
                    user.updateById();
                    ret.setCode(1000);
                    ret.setData(true);
                    ret.setMsg("密码重置成功");
                }
            }else{
                ret.setCode(1002);
                ret.setMsg("验证码不正确");
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/login")
    public R<JSONObject> login(@RequestParam(name = "mobile") String mobile,
                                @RequestParam(name = "password") String password){
        JSONObject retJson = new JSONObject();
        R<JSONObject> ret = new R<>();
        if(!StringUtils.isEmpty(mobile) && !StringUtils.isEmpty(password)){
            StickUser user = userService.login(mobile,password);
            if(user != null){
                String jwtToken = JwtHelper.generateToken(mobile,user.getUserId());
                retJson.put("token", jwtToken);
                ret.setCode(1000);
                ret.setMsg("登录成功");

                StickDevice deviceCondition = new StickDevice();
                deviceCondition.setUserId(user.getUserId());
                StickDevice device = deviceService.selectOne(new EntityWrapper<>(deviceCondition));
                if(device!=null){
                    retJson.put("bindimei", device.getDeviceImei());
                    retJson.put("phone",device.getBindPhone());
                    retJson.put("love", user.getLove());
                    ret.setData(retJson);
                }
            }else{
                ret.setCode(1001);
                ret.setMsg("登录失败，请检查手机号码和密码是否正确");
            }
        }
        ret.setData(retJson);
        return ret;
    }

    @PostMapping(value = "/api/auth/changePass")
    public R<Boolean> changePass(@RequestParam String oldpass, @RequestParam String newpass){
        String mobile = getMobile();
        R<Boolean> ret = new R<>(false);
        StickUser user = userService.selectByMobile(mobile);
        if(!StringUtils.isEmpty(oldpass) && !StringUtils.isEmpty(newpass)){
            StickUser existUser = userService.login(mobile, oldpass);
            if(existUser == null){
                ret.setCode(1001);
                ret.setMsg("旧密码错误");
                return ret;
            }else{
                existUser.setPassword(newpass);
                existUser.updateById();
                ret.setCode(1000);
                ret.setMsg("修改密码成功");
                ret.setData(true);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getDevice")
    public R<StickDevice> getDevice(@RequestParam String imei){
        R<StickDevice> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                ret.setCode(1000);
                ret.setData(device);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/addDevice")
    public R<JSONObject> addDevice(@RequestParam String imei){
        JSONObject retJson = new JSONObject();
        R<JSONObject> ret = new R<>();
        String mobile = getMobile();
        StickUser user = userService.selectByMobile(mobile);
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null && device.getUserId() != null && device.getUserId() > 0){
                ret.setCode(1003);
                ret.setMsg("手杖已被绑定");
            } else if(device == null) {
                ret.setCode(1004);
                ret.setMsg("手杖不存在");
            } else if(device != null) {
                device.setUserId(user.getUserId());
                device.setBindTime(new Date());
                device.updateById();
                ret.setCode(1000);
                ret.setMsg("手杖绑定成功");
            }
        }
        ret.setData(retJson);
        return ret;
    }

    @PostMapping(value = "/api/auth/bindPhone")
    public R<Boolean> bindPhone(@RequestParam String imei, @RequestParam String phone){
        R<Boolean> ret = new R<>(false);
        if(!StringUtils.isEmpty(imei) && !StringUtils.isEmpty(phone)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device == null){
                ret.setCode(1001);
                ret.setMsg("设备不存在");
                return ret;
            }else {
                device.setBindPhone(phone);
                ret.setCode(1000);
                ret.setMsg("绑定终端号码成功");
                ret.setData(true);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/saveDevice")
    public R<Boolean> saveDevice(@RequestBody JSONObject deviceInfo){
        R<Boolean> ret = new R<>(false);
        String imei = deviceInfo.getString("imei");
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device == null){
                ret.setCode(1001);
                ret.setMsg("设备不存在");
                return ret;
            }else {
                if(deviceInfo.containsKey("height")){
                    device.setHeight(deviceInfo.getInteger("height"));
                }
                if(deviceInfo.containsKey("weight")){
                    device.setWeight(deviceInfo.getInteger("weight"));
                }
                if(deviceInfo.containsKey("nickname")){
                    device.setNickName(deviceInfo.getString("nickname"));
                }
                if(deviceInfo.containsKey("avaster")){
                    device.setAvaster(deviceInfo.getString("avaster"));
                }
                if(deviceInfo.containsKey("city")){
                    device.setCity(deviceInfo.getString("city"));
                }
                device.updateById();
                ret.setCode(1000);
                ret.setMsg("设备信息保存成功");
                ret.setData(true);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getSosList")
    public R<JSONArray> getSosList(@RequestParam String imei){
        R<JSONArray> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device == null){
                ret.setCode(1001);
                ret.setMsg("设备不存在");
                return ret;
            }else {
                String sosList = device.getSosList();
                if(!StringUtils.isEmpty(sosList)){
                    JSONArray arry = JSONArray.parseArray(sosList);
                    ret.setData(arry);
                }
                ret.setCode(1000);
                ret.setMsg("获取sosList成功");
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/saveSosList")
    public R<Boolean> saveSosList(@RequestBody JSONObject deviceInfo){
        R<Boolean> ret = new R<>(false);
        String imei = deviceInfo.getString("imei");
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device == null){
                ret.setCode(1001);
                ret.setMsg("设备不存在");
                return ret;
            }else {
                JSONArray array = deviceInfo.getJSONArray("soslist");
                device.setSosList(array.toJSONString());
                device.updateById();
                ret.setCode(1000);
                ret.setData(true);
                ret.setMsg("设置SOSList成功");
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/userDevices")
    public R<JSONArray> userDevices(){
        R<JSONArray> ret = new R<>();
        String mobile = getMobile();
        StickUser user = userService.selectByMobile(mobile);
        if(!StringUtils.isEmpty(mobile)){
            List<StickDevice> devices = deviceService.listDeviceByUserId(user.getUserId());
            if(devices!=null && devices.size()>0){
                JSONArray array= JSONArray.parseArray(JSON.toJSONString(devices));
                ret.setData(array);
                ret.setCode(1000);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/setDefault")
    public R<JSONArray> setDefault(@RequestParam String imei){
        R<JSONArray> ret = new R<>();
        String mobile = getMobile();
        StickUser user = userService.selectByMobile(mobile);
        if(!StringUtils.isEmpty(mobile)){
            List<StickDevice> devices = deviceService.listDeviceByUserId(user.getUserId());
            if(devices!=null && devices.size()>0){
                for(StickDevice device:devices){
                    if(imei.equals(device.getDeviceImei())){
                        device.setUserDefault(true);
                    }else{
                        device.setUserDefault(false);
                    }
                    device.updateById();
                }
                JSONArray array= JSONArray.parseArray(JSON.toJSONString(devices));
                ret.setData(array);
                ret.setCode(1000);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/removeDevice")
    public R<JSONArray> removeDevice(@RequestParam String imei){
        R<JSONArray> ret = new R<>();
        String mobile = getMobile();
        StickUser user = userService.selectByMobile(mobile);
        if(!StringUtils.isEmpty(mobile)){
            StickDevice dev = deviceService.findDeviceByImei(imei);
            if(dev!=null){
                dev.setUserId(0);
                dev.setBindTime(null);
                dev.setUserDefault(false);
                dev.setSosList(null);
                dev.setNickName(null);
                dev.setWeight(0);
                dev.setAvaster(null);
                dev.setBindPhone(null);
                dev.setCity(null);
                dev.setAge(0);
                dev.setSex(0);
                dev.updateById();
            }
            List<StickDevice> devices = deviceService.listDeviceByUserId(user.getUserId());
            if(devices!=null && devices.size()>0){
                JSONArray array= JSONArray.parseArray(JSON.toJSONString(devices));
                ret.setData(array);
                ret.setCode(1000);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getGPS")
    public R<StickGPS> getGPS(@RequestParam String imei){
        R<StickGPS> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            //先发送定位指令
            stickService.startLocation(imei);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickGPS gps = gpsService.getLatestGPS(device.getDeviceId());
                ret.setCode(1000);
                ret.setData(gps);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/sendHealthCommand")
    public R<StickHeartBlood> sendHealth(@RequestParam String imei){
        R<StickHeartBlood> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            //先发送定位指令
            stickService.startLocation(imei);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickHeartBlood heartBlood = heartBloodService.getLatestHeartBlood(device.getDeviceId());
                ret.setCode(1000);
                ret.setData(heartBlood);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getHealthAll")
    public R<List<StickHeartBlood>> getHealthAll(@RequestParam String imei){
        R<List<StickHeartBlood>> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickHeartBlood cond = new StickHeartBlood();
                cond.setDeviceId(device.getDeviceId());
                List<StickHeartBlood> gps = heartBloodService.selectList(new EntityWrapper<>(cond).orderBy("add_time", false));
                ret.setCode(1000);
                ret.setData(gps);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getGPSByDate")
    public R<List<StickGPS>> getGPSByDate(@RequestParam String imei, @RequestParam String date){
        R<List<StickGPS>> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickGPS cond = new StickGPS();
                cond.setDeviceId(device.getDeviceId());
                List<StickGPS> gps = gpsService.selectList(new EntityWrapper<StickGPS>().where(" device_id = ? and left(gps_time,7) = ? ", device.getDeviceId(), date));
                ret.setCode(1000);
                ret.setData(gps);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getWarns")
    public R<List<StickWarn>> getWarns(@RequestParam String imei){
        R<List<StickWarn>> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickWarn cond = new StickWarn();
                cond.setDeviceId(device.getDeviceId());
                List<StickWarn> gps = warnService.selectList(new EntityWrapper<>(cond).orderBy("warn_time", false));
                ret.setCode(1000);
                ret.setData(gps);
            }
        }
        return ret;
    }

    @GetMapping(value = "/api/auth/getFences")
    public R<List<StickFence>> getFences(@RequestParam String imei){
        R<List<StickFence>> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickFence cond = new StickFence();
                cond.setDeviceId(device.getDeviceId());
                List<StickFence> gps = fenceService.selectList(new EntityWrapper<>(cond));
                ret.setCode(1000);
                ret.setData(gps);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/saveFence")
    public R<Boolean> saveFence(@RequestBody JSONObject fenceInfo){
        R<Boolean> ret = new R<>(false);
        String imei = fenceInfo.getString("imei");
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device == null){
                ret.setCode(1001);
                ret.setMsg("设备不存在");
                return ret;
            }else {
                StickFence fence = null;
                if(fenceInfo.containsKey("fenceId")){
                    fence = fenceService.selectById(fenceInfo.getInteger("fenceId"));
                }else {
                    fence = new StickFence();
                }
                fence.setAddTime(new Date());
                fence.setDeviceId(device.getDeviceId());
                if(fenceInfo.containsKey("name")){
                    fence.setFenceName(fenceInfo.getString("name"));
                }
                if(fenceInfo.containsKey("radius")){
                    fence.setRadius(fenceInfo.getInteger("radius"));
                }
                if(fenceInfo.containsKey("latitute")){
                    fence.setGpsLatitude(fenceInfo.getDouble("latitute"));
                }
                if(fenceInfo.containsKey("longtitute")){
                    fence.setGpsLongitude(fenceInfo.getDouble("longtitute"));
                }
                fence.insertOrUpdate();
                ret.setCode(1000);
                ret.setMsg("围栏保存成功");
                ret.setData(true);
            }
        }
        return ret;
    }

    /*
    @RequestMapping(value = "/api")
    public String api(@RequestParam(name = "arg1") String arg1,
                      @RequestParam(name = "arg2") String arg2){
        JSONObject retJson = new JSONObject();
        if(!StringUtils.isEmpty(arg1)){
            if("zhuce".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String nickName = paraObj.getString("mingzi");
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    StickUser user = new StickUser();
                    user.setMobile(mobile);
                    StickUser existUser = userService.login(mobile, password);
                    if (existUser != null && existUser.getUserId() > 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "注册失败，手机号码已存在");
                    } else {
                        user.setNickName(nickName);
                        user.setPassword(password);
                        user.setAddTime(new Date());
                        boolean isOK = userService.insert(user);
                        if (isOK) {
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "注册成功");
                        } else {
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "注册失败");
                        }
                    }
                }
            }else if("sendcode".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("phone");
                    if(!StringUtils.isEmpty(mobile)){

                        String code = (String) redisTemplate.opsForValue().get("code" + mobile);
                        String randomNumber = RandomUtil.randomNumbers(6);
                        // 缓存中不存在code
                        if(StringUtils.isEmpty(code)){
                            redisTemplate.opsForValue().set("code" + mobile, randomNumber,120, TimeUnit.SECONDS);
                            final String accessKeyId = smsTemplateConfig.getAccessKey();
                            final String accessKeySecret = smsTemplateConfig.getSecretKey();
                            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
                            IAcsClient client = new DefaultAcsClient(profile);

                            CommonRequest request = new CommonRequest();
                            //request.setProtocol(ProtocolType.HTTPS);
                            request.setMethod(MethodType.POST);
                            request.setDomain("dysmsapi.aliyuncs.com");
                            request.setVersion("2017-05-25");
                            request.setAction("SendSms");
                            request.putQueryParameter("RegionId", "cn-hangzhou");
                            request.putQueryParameter("PhoneNumbers", mobile);
                            request.putQueryParameter("SignName", "智能手杖");
                            request.putQueryParameter("TemplateCode", smsTemplateConfig.getTemplateCode());
                            request.putQueryParameter("TemplateParam", "{\"code\":\""+randomNumber+"\"}");
                            try {
                                CommonResponse response = client.getCommonResponse(request);
                                JSONObject ret = JSON.parseObject(response.getData());
                                String msg =  ret.getString("Message");
                                if("OK".equals(msg)){
                                    retJson.put("retcode", 1000);
                                    retJson.put("msg", "短信发送成功");
                                }else{
                                    retJson.put("retcode", 1002);
                                    retJson.put("msg", "短信发送失败");
                                }
                            }catch (ServerException e) {
                                e.printStackTrace();
                                retJson.put("retcode", 1002);
                                retJson.put("msg", "短信发送失败");
                            } catch (ClientException e) {
                                e.printStackTrace();
                                retJson.put("retcode", 1002);
                                retJson.put("msg", "短信发送失败");
                            }
                        }else{
                            retJson.put("retcode", 1003);
                            retJson.put("msg", "请勿频繁发送");
                        }
                    }
                }
            }else if("forgetpassword".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("phone");
                    String checkcode = paraObj.getString("checkcode");
                    String newpassword = paraObj.getString("newpassword");
                    if(!StringUtils.isEmpty(mobile) && !StringUtils.isEmpty(checkcode) && !StringUtils.isEmpty(newpassword)){
                        String code = (String) redisTemplate.opsForValue().get("code" + mobile);
                        if(!StringUtils.isEmpty(code) && code.equals(checkcode)){
                            StickUser user = userService.selectByMobile(mobile);
                            if(user == null){
                                retJson.put("retcode", 1003);
                                retJson.put("msg", "用户不存在");
                            }else{
                                user.setPassword(newpassword);
                                retJson.put("retcode", 1000);
                                retJson.put("msg", "密码重置成功");
                            }
                        }else{
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "验证码不正确");
                        }
                    }
                }
            }else if("xiugaimima".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String newpassword = paraObj.getString("xinmima");
                    if(!StringUtils.isEmpty(mobile) && !StringUtils.isEmpty(password) && !StringUtils.isEmpty(newpassword)){
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            user.setPassword(newpassword);
                            user.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "修改密码成功");
                        }
                    }
                }
            }else if("denglu".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    StickUser condition = new StickUser();
                    condition.setMobile(mobile);
                    condition.setPassword(password);
                    StickUser user = userService.selectOne(new EntityWrapper<>(condition));
                    if(user != null){
                        retJson.put("retcode", 1000);
                        StickDevice deviceCondition = new StickDevice();
                        deviceCondition.setUserId(user.getUserId());
                        StickDevice device = deviceService.selectOne(new EntityWrapper<>(deviceCondition));
                        if(device!=null){
                            retJson.put("bindimei", device.getDeviceImei());
                            retJson.put("phone",device.getBindPhone());
                            retJson.put("love", user.getLove());
                            retJson.put("msg", "登录成功");
                        }
                    }else{
                        retJson.put("retcode", 1001);
                        retJson.put("msg", "登录失败，请检查手机号码和密码是否正确");
                    }
                }
            }else if("tianjiashebei".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    StickUser user = userService.login(mobile,password);
                    if(user == null || user.getUserId() == 0){
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else {
                        if(!StringUtils.isEmpty(imei)){
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null && device.getUserId() > 0){
                                retJson.put("retcode", 1003);
                                retJson.put("msg", "手杖已被绑定");
                            } else if(device == null) {
                                retJson.put("retcode", 1004);
                                retJson.put("msg", "手杖不存在");
                            } else if(device != null) {
                                device.setUserId(user.getUserId());
                                device.updateById();
                                retJson.put("retcode", 1008);
                                retJson.put("msg", "手杖绑定成功");
                            }
                        }
                    }


                }
            }else if("shebeishoujihao".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String phone = paraObj.getString("shoujihao");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device!=null){
                            device.setBindPhone(phone);
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "手杖绑定手机号码成功");
                        }
                    }
                }
            } else if("citysave".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String city = paraObj.getString("city");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setCity(city);
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "保存手杖城市成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            } else if("heightsave".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String height = paraObj.getString("height");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setHeight(Integer.parseInt(height));
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "保存身高成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            } else if("weightsave".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String weight = paraObj.getString("weight");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setWeight(Integer.parseInt(weight));
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "保存体重成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            } else if("nicknamesave".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String nickname = paraObj.getString("nickname");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setNickName(nickname);
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "保存昵称成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            } else if("saveheaderimg".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String content = paraObj.getString("content");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setAvaster(content);
                            device.updateById();
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "保存用户头像成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            }else if("dedaoqinqinghaoma".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            String sosList = device.getSosList();
                            if(StringUtils.isEmpty(sosList)){
                                JSONArray arry = JSONArray.parseArray(sosList);
                                for(Object p:arry){
                                    JSONObject phone = (JSONObject)p;
                                    for(String key:phone.keySet()){
                                        retJson.put(key, phone.get(key));
                                    }
                                }
                            }
                            retJson.put("switchonoff", device.getSwitchOnOff());
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "获取亲情号码成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            }else if("dedaososhaoma".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            String sosList = device.getSosList();
                            if(StringUtils.isEmpty(sosList)){
                                JSONArray arry = JSONArray.parseArray(sosList);
                                for(Object p:arry){
                                    JSONObject phone = (JSONObject)p;
                                    for(String key:phone.keySet()){
                                        if("sosname1".equals(key)){
                                            retJson.put("sosname01", phone.get(key));
                                        }
                                        if("sosname2".equals(key)){
                                            retJson.put("sosname02", phone.get(key));
                                        }
                                        if("sosname3".equals(key)){
                                            retJson.put("sosname03", phone.get(key));
                                        }
                                        if("sosname4".equals(key)){
                                            retJson.put("sosname04", phone.get(key));
                                        }
                                        if("sos01".equals(key)){
                                            retJson.put("sos01", phone.get(key));
                                        }
                                        if("sos02".equals(key)){
                                            retJson.put("sos02", phone.get(key));
                                        }
                                        if("sos03".equals(key)){
                                            retJson.put("sos03", phone.get(key));
                                        }
                                        if("sos04".equals(key)){
                                            retJson.put("sos04", phone.get(key));
                                        }
                                    }
                                }
                            }
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "获取亲情号码成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            }else if("qinqinghaoma".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String phone01 = paraObj.getString("phone01");
                    String phone02 = paraObj.getString("phone02");
                    String phone03 = paraObj.getString("phone03");
                    String phonename01 = paraObj.getString("phonename01");
                    String phonename02 = paraObj.getString("phonename02");
                    String phonename03 = paraObj.getString("phonename03");
                    String switchonoff = paraObj.getString("switchonoff");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            JSONArray phoneListArr = new JSONArray();
                            if(StringUtils.isEmpty(phone01)) {
                                JSONObject p = new JSONObject();
                                p.put("phonename01", phonename01);
                                p.put("phone01", phone01);
                                phoneListArr.add(p);
                            }
                            if(StringUtils.isEmpty(phone02)) {
                                JSONObject p = new JSONObject();
                                p.put("phonename02", phonename02);
                                p.put("phone02", phone02);
                                phoneListArr.add(p);
                            }
                            if(StringUtils.isEmpty(phone03)) {
                                JSONObject p = new JSONObject();
                                p.put("phonename03", phonename03);
                                p.put("phone03", phone03);
                                phoneListArr.add(p);
                            }
                            device.setSosList(phoneListArr.toJSONString());
                            device.setSwitchOnOff(Integer.parseInt(switchonoff));
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "设置亲情号码成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            }else if("soshaoma".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    String phone01 = paraObj.getString("sos1");
                    String phone02 = paraObj.getString("sos2");
                    String phone03 = paraObj.getString("sos3");
                    String phone04 = paraObj.getString("sos4");
                    String phonename01 = paraObj.getString("sosname1");
                    String phonename02 = paraObj.getString("sosname2");
                    String phonename03 = paraObj.getString("sosname3");
                    String phonename04 = paraObj.getString("sosname4");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            JSONArray phoneListArr = new JSONArray();
                            if(StringUtils.isEmpty(phone01)) {
                                JSONObject p = new JSONObject();
                                p.put("sosname1", phonename01);
                                p.put("sos1", phone01);
                                phoneListArr.add(p);
                            }
                            if(StringUtils.isEmpty(phone02)) {
                                JSONObject p = new JSONObject();
                                p.put("sosname2", phonename02);
                                p.put("sos2", phone02);
                                phoneListArr.add(p);
                            }
                            if(StringUtils.isEmpty(phone03)) {
                                JSONObject p = new JSONObject();
                                p.put("sosname3", phonename03);
                                p.put("sos3", phone03);
                                phoneListArr.add(p);
                            }
                            if(StringUtils.isEmpty(phone04)) {
                                JSONObject p = new JSONObject();
                                p.put("sosname4", phonename04);
                                p.put("sos4", phone04);
                                phoneListArr.add(p);
                            }
                            device.setSosList(phoneListArr.toJSONString());
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "设置亲情号码成功");
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                        }
                    }
                }
            } else if("bangdingshebeiguanli".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setUserDefault(true);
                            device.updateById();
                            List<StickDevice> deviceList = deviceService.listDeviceByUserId(user.getUserId());
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "默认关注设置成功");
                            retJson.put("devices", JSONArray.parseArray(JSON.toJSONString(deviceList)));
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                            List<StickDevice> deviceList = deviceService.listDeviceByUserId(user.getUserId());
                            retJson.put("devices", JSONArray.parseArray(JSON.toJSONString(deviceList)));
                        }
                    }
                }
            } else if("shanchushebeiguanli".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    String imei = paraObj.getString("imei");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        StickDevice device = deviceService.findDeviceByImei(imei);
                        if(device != null){
                            device.setUserId(0);
                            device.setBindPhone("");
                            device.setBindTime(null);
                            device.setUserDefault(false);
                            device.updateById();
                            List<StickDevice> deviceList = deviceService.listDeviceByUserId(user.getUserId());
                            retJson.put("retcode", 1000);
                            retJson.put("msg", "删除设备成功");
                            retJson.put("devices", JSONArray.parseArray(JSON.toJSONString(deviceList)));
                        }else{
                            retJson.put("retcode", 1001);
                            retJson.put("msg", "手杖不存在");
                            List<StickDevice> deviceList = deviceService.listDeviceByUserId(user.getUserId());
                            retJson.put("devices", JSONArray.parseArray(JSON.toJSONString(deviceList)));
                        }
                    }
                }
            }  else if("shebeiliebiaoguanli".equals(arg1)){
                if(!StringUtils.isEmpty(arg2)) {
                    JSONObject paraObj = JSONObject.parseObject(arg2);
                    String mobile = paraObj.getString("yonghuming");
                    String password = paraObj.getString("mima");
                    StickUser user = userService.login(mobile, password);
                    if (user == null || user.getUserId() == 0) {
                        retJson.put("retcode", 1002);
                        retJson.put("msg", "用户名密码错误，请重新登录");
                    }else{
                        List<StickDevice> devices = deviceService.listDeviceByUserId(user.getUserId());
                        retJson.put("retcode", 1000);
                        retJson.put("msg", "获取用户设备成功");
                        if(devices != null && devices.size() > 0){
                            JSONArray array= JSONArray.parseArray(JSON.toJSONString(devices));
                            retJson.put("devices", array);
                        }else{
                            retJson.put("devices", "[]");
                        }
                    }
                } else if("shishidingwei".equals(arg1)){
                    if(!StringUtils.isEmpty(arg2)) {
                        JSONObject paraObj = JSONObject.parseObject(arg2);
                        String mobile = paraObj.getString("yonghuming");
                        String password = paraObj.getString("mima");
                        String imei = paraObj.getString("imei");
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null){
                                StickGPS gps = gpsService.getLatestGPS(device.getDeviceId());
                                if(gps != null){
                                    String locationTime = DateUtil.format(gps.getGpsTime(), "YYYY-MM-dd HH:mm:ss");
                                    String locationType = gps.getLocationType();
                                    String locationAddress = gps.getAddress();
                                    Double latitute = gps.getLatitude();
                                    Double longtitute = gps.getLongitude();
                                    String name = device.getNickName();
                                    String battery = gps.getBattery();
                                    retJson.put("retcode", 1000);
                                    retJson.put("longitude", longtitute);
                                    retJson.put("latitude",latitute);
                                    retJson.put("locationtime",locationTime);
                                    retJson.put("locationtype",locationType);
                                    retJson.put("name",name);
                                    retJson.put("locationaddress",locationAddress);
                                    retJson.put("battery",battery);
                                    retJson.put("msg", "获取实时定位成功");
                                }else {
                                    retJson.put("retcode", 1002);
                                    retJson.put("msg", "定位失败");
                                }

                            }else{
                                retJson.put("retcode", 1001);
                                retJson.put("msg", "手杖不存在");
                            }
                        }
                    }
                } else if("gethealthlistall".equals(arg1)){
                    if(!StringUtils.isEmpty(arg2)) {
                        JSONObject paraObj = JSONObject.parseObject(arg2);
                        String mobile = paraObj.getString("yonghuming");
                        String password = paraObj.getString("mima");
                        String imei = paraObj.getString("imei");
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null){
                                StickHeartBlood cond = new StickHeartBlood();
                                cond.setDeviceId(device.getDeviceId());
                                List<StickHeartBlood> list = heartBloodService.selectList(new EntityWrapper<>(cond));
                                JSONArray arry = new JSONArray();
                                for(StickHeartBlood d:list){
                                    JSONObject obj = new JSONObject();
                                    obj.put("date", DateUtil.format(d.getAddTime(), "YYYY-MM-dd"));
                                    obj.put("time", DateUtil.format(d.getAddTime(), "HH:mm:ss"));
                                    obj.put("heartrate", d.getHeartRate());
                                    String bloodPressure = d.getBloodPressure();
                                    if(!StringUtils.isEmpty(bloodPressure)) {
                                        //舒张压
                                        obj.put("diastolic", bloodPressure.split("/")[0]);
                                        //收缩压
                                        obj.put("systolic",  bloodPressure.split("/")[1]);
                                    }
                                    arry.add(obj);
                                }
                                retJson.put("healthlist", arry.toJSONString());
                                retJson.put("retcode", 1000);
                                retJson.put("msg", "获取健康数据成功");
                            }else{
                                retJson.put("retcode", 1001);
                                retJson.put("msg", "手杖不存在");
                            }
                        }
                    }
                } else if("healthcommand".equals(arg1)){
                    if(!StringUtils.isEmpty(arg2)) {
                        JSONObject paraObj = JSONObject.parseObject(arg2);
                        String mobile = paraObj.getString("yonghuming");
                        String password = paraObj.getString("mima");
                        String imei = paraObj.getString("imei");
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null){
                                //TODO 向设备发送测量指令
                                retJson.put("retcode", 1000);
                                retJson.put("msg", "指令发送成功");

                            }else{
                                retJson.put("retcode", 1001);
                                retJson.put("msg", "手杖不存在");
                            }
                        }
                    }
                } else if("lishiguiji".equals(arg1)){
                    if(!StringUtils.isEmpty(arg2)) {
                        JSONObject paraObj = JSONObject.parseObject(arg2);
                        String mobile = paraObj.getString("yonghuming");
                        String password = paraObj.getString("mima");
                        String imei = paraObj.getString("imei");
                        String date = paraObj.getString("date");
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null){
                                JSONObject allObj = new JSONObject();
                                StickGPS cond = new StickGPS();
                                cond.setDeviceId(device.getDeviceId());
                                List<StickGPS> gpsList = gpsService.selectList(new EntityWrapper<>(cond).eq("left(add_time,7)", date));
                                if(gpsList != null){
                                    JSONArray array = new JSONArray();
                                    for(StickGPS gps: gpsList){
                                        JSONObject obj = new JSONObject();
                                        obj.put("time", DateUtil.format(gps.getGpsTime(),"YYYY-MM-dd HH:mm:ss"));
                                        obj.put("type", gps.getLocationType());
                                        obj.put("longitude", gps.getLongitude());
                                        obj.put("latitude", gps.getLatitude());
                                        array.add(obj);
                                    }
                                    allObj.put(date+".lsgj", array.toJSONString());
                                }
                                retJson.put("lishiguiji", allObj.toJSONString());
                                retJson.put("retcode", 1000);
                                retJson.put("msg", "获取历史轨迹数据成功");

                            }else{
                                retJson.put("retcode", 1001);
                                retJson.put("msg", "手杖不存在");
                            }
                        }
                    }
                } else if("xiaoxizhongxin".equals(arg1)){
                    if(!StringUtils.isEmpty(arg2)) {
                        JSONObject paraObj = JSONObject.parseObject(arg2);
                        String mobile = paraObj.getString("yonghuming");
                        String password = paraObj.getString("mima");
                        String imei = paraObj.getString("imei");
                        StickUser user = userService.login(mobile, password);
                        if (user == null || user.getUserId() == 0) {
                            retJson.put("retcode", 1002);
                            retJson.put("msg", "用户名密码错误，请重新登录");
                        }else{
                            StickDevice device = deviceService.findDeviceByImei(imei);
                            if(device != null){
                                StickWarn cond = new StickWarn();
                                cond.setDeviceId(device.getDeviceId());
                                List<StickWarn> warnList = warnService.selectList(new EntityWrapper<>(cond).orderBy("warn_time", false));
                                if(warnList != null){
                                    JSONArray array = new JSONArray();
                                    for(StickWarn warn: warnList){
                                        JSONObject obj = new JSONObject();
                                        obj.put("time", DateUtil.format(warn.getWarnTime(),"YYYY-MM-dd HH:mm:ss"));
                                        obj.put("type", warn.getWarnType());
                                        obj.put("content",warn.getContent());
                                        array.add(obj);
                                    }
                                    retJson.put("xiaoxizhongxin", array.toJSONString());
                                }

                                retJson.put("retcode", 1000);
                                retJson.put("msg", "获取告警数据成功");

                            }else{
                                retJson.put("retcode", 1001);
                                retJson.put("msg", "手杖不存在");
                            }
                        }
                    }
                }
            }
        }
        return retJson.toJSONString();
    }
    */

    @PostMapping(value = "/rec")
    public boolean receive(@RequestParam(name = "cmd") String cmd,
                           @RequestParam(name = "imei") String imei,
                           @RequestParam(name = "data") String data){
        if(StringUtils.isEmpty(imei) || StringUtils.isEmpty("cmd") || StringUtils.isEmpty(data)){
            return false;
        }

        StickDevice device = deviceService.findDeviceByImei(imei);
        if(device == null){
            return false;
        }
        if("GPS".equals(cmd)){
            JSONObject dataObj = JSONObject.parseObject(data);
            if (dataObj != null && !dataObj.isEmpty()) {
                double longtitute = dataObj.getDouble("longtitude");
                double latitute = dataObj.getDouble("latitute");
                String direction = dataObj.getString("direction");
                String speed = dataObj.getString("speed");
                String satellite = dataObj.getString("satellite");
                String battery = dataObj.getString("battery");
                String signal = dataObj.getString("signal");
                StickGPS gps = new StickGPS();
                gps.setLongitude(longtitute);
                gps.setLatitude(latitute);
                gps.setDeviceId(device.getDeviceId());
                gps.setDirection(direction);
                gps.setGpsTime(new Date());
                gps.setSatellite(satellite);
                gps.setBattery(battery);
                gps.setSignal(signal);
                gps.setSpeed(speed);
                gps.insert();
            }
        }else if("FALLDOWN".equals(cmd)){
            StickWarn warn = new StickWarn();
            warn.setDeviceId(device.getDeviceId());
            warn.setWarnTime(new Date());
            warn.setWarnType(1);
            warn.setContent(data);
            warn.insert();
        }else if("BLOODPRESS".equals(cmd)){
            StickHeartBlood stickHeartBlood = new StickHeartBlood();
            stickHeartBlood.setDeviceId(device.getDeviceId());
            stickHeartBlood.setAddTime(new Date());
            String[] datas = data.split(",");
            stickHeartBlood.setHeartRate(Integer.parseInt(datas[0]));
            stickHeartBlood.setBloodPressure(datas[1]+"/"+datas[2]);
            stickHeartBlood.insert();
        }
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
