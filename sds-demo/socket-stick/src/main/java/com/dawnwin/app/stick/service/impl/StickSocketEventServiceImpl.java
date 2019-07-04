package com.dawnwin.app.stick.service.impl;

import com.codingapi.sds.socket.service.SocketControl;
import com.codingapi.sds.socket.service.SocketEventService;
import com.dawnwin.app.stick.model.StickCommand;
import com.dawnwin.app.stick.service.StickService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * create by lorne on 2017/10/13
 */
@Service
public class StickSocketEventServiceImpl implements SocketEventService {

    @Autowired
    private SocketControl socketControl;

    @Autowired
    private StickService stickService;

    private Logger logger = LoggerFactory.getLogger(StickSocketEventServiceImpl.class);

    @Override
    public void onReadListener(ChannelHandlerContext ctx, String uniqueKey, Object msg) {

        byte[] datas = (byte[]) msg;

        String modelName = socketControl.getModelName();

        String cmdStr = new String(datas);
        logger.info("onReadListener--> modelName->"+modelName+",uniqueKey->"+uniqueKey+",msg->"+cmdStr);
        String response = "";
        if(!StringUtils.isEmpty(cmdStr)) {
            StickCommand command = StickCommand.getCommand(cmdStr);
            if(command != null){
                if(StickCommand.DEVICE_LOGIN.equals(command.getCmd())){
                    if(!stickService.checkDevice(command.getDeviceImei())){
                        ctx.close();
                    }
                }
                //重置心跳时间规则
                if(StickCommand.DEVICE_HEARTBEAT.equals(command.getCmd())) {
                    socketControl.resetHeartTime(ctx.channel(),180);
                }
                //设备主动发起的指令
                response = stickService.processStickCommand(modelName,uniqueKey,command);
            }else{
                //设备回复的指令
            }
        }

        response+="\r\n";
        ctx.writeAndFlush(response.getBytes());
    }

    @Override
    public void onConnectionListener(ChannelHandlerContext ctx, String uniqueKey) {
        String modelName = socketControl.getModelName();
        logger.info("onConnectionListener--> modelName->"+modelName+",uniqueKey->"+uniqueKey);
    }

    @Override
    public void onDisConnectionListener(ChannelHandlerContext ctx, String uniqueKey) {
        String modelName = socketControl.getModelName();
        logger.info("onDisConnectionListener--> modelName->"+modelName+",uniqueKey->"+uniqueKey);
    }

    @Override
    public void onHeartNoWriteDataListener(ChannelHandlerContext ctx, String uniqueKey) {
        logger.info("onHeartNoWriteDataListener");
        //ctx.close();
    }

    @Override
    public void onHeartNoReadDataListener(ChannelHandlerContext ctx, String uniqueKey) {
        logger.info("onHeartNoReadDataListener");
        //ctx.close();
    }

    @Override
    public boolean hasOpenHeartCheck() {
        //开启心跳检测
        return true;
    }
}
