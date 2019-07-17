package com.dawnwin.stick.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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
    @Autowired
    private RestTemplate restTemplate;

    private String getMobile(){
        String mobile = (String) request.getAttribute("mobile");
        return mobile;
    }

    @RequestMapping(value = "/api/index")
    public boolean index(){
        return true;
    }

    @GetMapping(value = "/check")
    public boolean check(@RequestParam String imei){
        if(!StringUtils.isEmpty(imei)){
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                return true;
            }
        }
        return false;
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
                device.updateById();
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
                if(deviceInfo.containsKey("sex")){
                    device.setSex(deviceInfo.getInteger("sex"));
                }
                if(deviceInfo.containsKey("age")){
                    device.setAge(deviceInfo.getInteger("age"));
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
            stickService.startMeasure(imei);
            StickDevice device = deviceService.findDeviceByImei(imei);
            if(device != null){
                StickHeartBlood heartBlood = heartBloodService.getLatestHeartBlood(device.getDeviceId());
                ret.setCode(1000);
                ret.setData(heartBlood);
            }
        }
        return ret;
    }

    @PostMapping(value = "/api/auth/sendCommand")
    public R<Boolean> sendCommand(@RequestParam String imei,@RequestParam String cmd, @RequestParam String data){
        R<Boolean> ret = new R<>();
        if(!StringUtils.isEmpty(imei)){
            stickService.startMeasure(imei);
            if(StringUtils.isEmpty(imei) || StringUtils.isEmpty("cmd")){
                ret.setData(false);
            }
            if("SOSLIST".equals(cmd)){
                stickService.setSosList(imei, data);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("MONITOR".equals(cmd)){
                stickService.startMonitor(imei, data);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("WEBLOCATION".equals(cmd)){
                stickService.startLocation(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("SHUTDOWN".equals(cmd)){
                stickService.shutdownStick(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("BLOODPRESS".equals(cmd) || "WEBHEALTH".equals(cmd)){
                stickService.startMeasure(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("RESET".equals(cmd)){
                stickService.resetStick(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("OPENGPS".equals(cmd)){
                stickService.openGPS(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("CLOSEGPS".equals(cmd)){
                stickService.closeGPS(imei);
                ret.setCode(1000);
                ret.setData(true);
            }
            if("INTERVAL".equals(cmd)){
                stickService.setInterval(imei, Integer.parseInt(data));
                ret.setCode(1000);
                ret.setData(true);
            }
            if("WIFIINTERVAL".equals(cmd)){
                stickService.setWifiInterval(imei, Integer.parseInt(data));
                ret.setCode(1000);
                ret.setData(true);
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
        if("GPS".equals(cmd) || "LBS".equals(cmd) || "WIFI".equals(cmd)){
            //这些数据怎么转换参考：http://www.cellocation.com/interfac/#cell
            StickGPS gps = new StickGPS();
            if("LBS".equals(cmd)){
                gps.setLocationType("3");
            }else if("WIFI".equals(cmd)){
                gps.setLocationType("1");
            }else if("GPS".equals(cmd)){
                gps.setLocationType("2");
            }
            gps.setDeviceId(device.getDeviceId());
            gps.setGpsTime(new Date());
            gps.setGpsData(data);
            if(!StringUtils.isEmpty(data)){
                JSONObject obj = restTemplate.getForObject("http://apilocate.amap.com/position?key=a19360c1294349ca021f32893658de66&accesstype=0"+data+"&output=json", JSONObject.class);
                if(obj != null && obj.containsKey("result")){
                    JSONObject locationObj = obj.getJSONObject("result");
                    gps.setAddress(locationObj.getString("desc"));
                    gps.setLatitude(Double.valueOf(locationObj.getString("location").split(",")[1]));
                    gps.setLongitude(Double.valueOf(locationObj.getString("location").split(",")[0]));
                    gps.setRadius(Integer.valueOf(locationObj.getString("radius")));
                }
            }
            gps.insert();
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
        if("BLOODPRESS".equals(cmd) || "WEBHEALTH".equals(cmd)){
            return stickService.startMeasure(imei);
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
        return false;
    }

}
