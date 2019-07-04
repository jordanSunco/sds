package com.dawnwin.app.stick.model;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
public class StickCommand {

    public static final String DEVICE_LOGIN = "LOGIN";
    public static final String DEVICE_HEARTBEAT = "HEARTBEAT";
    public static final String DEVICE_FALLDOWN = "FALLDOWN";
    public static final String DEVICE_BLOODPRESS = "BLOODPRESS";
    public static final String DEVICE_GPS_LBS = "LBS";
    public static final String DEVICE_GPS_WIFI = "WIFI";
    public static final String DEVICE_GPS_LBS_WIFI = "WIFI";
    public static final String DEVICE_GPS = "GPS";
    public static final String DEVICE_BLOOD_PRESS = "BLOODPRESS";
    public static final String SEND_WEBLOCATION = "WEBLOCATION";
    public static final String SEND_SOS_LIST = "SOSLIST";
    public static final String SEND_MONITOR = "MONITOR";
    public static final String SEND_SHUTDOWN = "SHUTDOWN";
    public static final String SEND_RESET = "RESET";
    public static final String SEND_OPENGPS = "OPENGPS";
    public static final String SEND_CLOSEGPS = "CLOSEGPS";
    public static final String SEND_INTERVAL = "INTERVAL";
    public static final String SEND_WIFIINTERVAL = "WIFIINTERVAL";

    private static List<String> validCmdList = Lists.newArrayList();

    static {
        validCmdList.add(DEVICE_LOGIN);
        validCmdList.add(DEVICE_HEARTBEAT);
        validCmdList.add(DEVICE_FALLDOWN);
        validCmdList.add(DEVICE_BLOODPRESS);
        validCmdList.add(DEVICE_GPS_LBS);
        validCmdList.add(DEVICE_GPS_WIFI);
        validCmdList.add(DEVICE_GPS_LBS_WIFI);
        validCmdList.add(DEVICE_GPS);
        validCmdList.add(DEVICE_BLOOD_PRESS);
    }


    private String cmd;
    private String productName;
    private String projectName;
    private String cmdVersion;
    private String deviceImei;
    private String cmdData;
    private String date;
    private String time;
    private String power;
    private String signal;


    public static boolean isStickCommand(String cmdStr){
        for(String cmd:validCmdList){
            if(cmdStr.contains(cmd)){
                return true;
            }
        }
        return false;
    }

    public static StickCommand getCommand(String cmdStr){
        if(StringUtils.isEmpty(cmdStr)||!cmdStr.startsWith("{")&&!cmdStr.endsWith("}") || !isStickCommand(cmdStr)){
            return null;
        }
        StickCommand command = new StickCommand();
        String[] splitItems = cmdStr.replace("{","").replace("}","").split("\\*");
        int len = splitItems.length;
        if(len>0 && !StringUtils.isEmpty(splitItems[0])){
            command.setProductName(splitItems[0]);
        }
        if(len>1 && !StringUtils.isEmpty(splitItems[1])){
            command.setProjectName(splitItems[1]);
        }
        if(len>2 &&!StringUtils.isEmpty(splitItems[2])){
            command.setCmdVersion(splitItems[2]);
        }
        if(len>3 &&!StringUtils.isEmpty(splitItems[3])){
            command.setDeviceImei(splitItems[3]);
        }
        if(len>4 &&!StringUtils.isEmpty(splitItems[4])){
            command.setDate(splitItems[4]);
        }
        if(len>5 &&!StringUtils.isEmpty(splitItems[5])){
            command.setTime(splitItems[5]);
        }
        if(len>6 &&!StringUtils.isEmpty(splitItems[6])){
            command.setPower(splitItems[6]);
        }
        if(len>7 &&!StringUtils.isEmpty(splitItems[7])){
            command.setSignal(splitItems[7]);
        }
        if(len>8 &&!StringUtils.isEmpty(splitItems[8])){
            command.setCmd(splitItems[8]);
        }
        if(len>9 &&!StringUtils.isEmpty(splitItems[9])){
            command.setCmdData(splitItems[9]);
        }

        return command;
    }
}
