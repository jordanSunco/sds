package com.dawnwin.stick.service.impl;

import com.codingapi.sds.delivery.model.Server;
import com.codingapi.sds.delivery.service.DeliveryServerSendEventService;
import com.codingapi.sds.delivery.service.DeliveryServerService;
import com.codingapi.sds.delivery.utils.SocketUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * create by lorne on 2017/10/13
 */
@Service
public class DeliveryServerSendEventServiceImpl implements DeliveryServerSendEventService {



    @Autowired
    private DeliveryServerService deliveryServerService;


    @Override
    public void onDeliveryListener(ChannelHandlerContext ctx, Object msg) {

        //均为演示demo 实际业务还需要自己处理

        //通过SDS获取有效的Socket服务
        Server server =  deliveryServerService.getOkServer();

        //发送分配的负载均衡信息给客户端
        SocketUtils.send(ctx.channel(),(server.toString()+"\r\n").getBytes());

        //发送完后直接断开这个socket连接
        ctx.close();
    }

    @Override
    public void onConnectionListener(ChannelHandlerContext ctx) {
        //建立连接业务
    }

    @Override
    public void onDisConnectionListener(ChannelHandlerContext ctx) {
        //是否连接业务
    }
}
