package com.codingapi.sds.socket.socket.handler;


import com.codingapi.sds.socket.service.SocketService;
import com.codingapi.sds.socket.utils.SocketManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by lorne on 2017/8/12.
 */
public class SocketHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(SocketHandler.class);


    private SocketService socketService;


    public SocketHandler(SocketService socketService) {
        this.socketService = socketService;
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx,final Object msg) { // (2)
        // Discard the received data silently.
        String uniqueKey = ctx.channel().remoteAddress().toString();

        socketService.getSocketEventService().onReadListener(ctx,uniqueKey,msg);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(SocketManager.getInstance().isAllowConnection()){
            logger.info("connection - > "+ctx);
            SocketManager.getInstance().addClient(ctx.channel());

            String uniqueKey = ctx.channel().remoteAddress().toString();

            //将数据存储到distribute下
            try {
                socketService.create(uniqueKey);

                socketService.getSocketEventService().onConnectionListener(ctx, uniqueKey);
            }catch (Exception e){
                logger.error("error - connection - > "+e.getMessage());
                ctx.close();
            }

        }else{

            logger.info("not allow connection - > "+ctx+
                    ",nowConnectionSize->"+SocketManager.getInstance().getNowConnection()+
                    ",maxConnectionSize->"+SocketManager.getInstance().getMaxConnection());

            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("disconnection - > "+ctx);
        String uniqueKey = ctx.channel().remoteAddress().toString();
        socketService.getSocketEventService().onDisConnectionListener(ctx,uniqueKey);
        //channel失效，从Map中移除
        SocketManager.getInstance().removeClient(ctx.channel());

        try {
            //将数据从distribute下异常
            socketService.remove(uniqueKey);
        }catch (Exception e){
            logger.error("error - disconnection - > "+e.getMessage());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())&& socketService.getSocketEventService().hasOpenHeartCheck()) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String uniqueKey = ctx.channel().remoteAddress().toString();
            if (event.state() == IdleState.READER_IDLE) {
                //表示已经多久没有收到数据了

                socketService.getSocketEventService().onHeartNoReadDataListener(ctx,uniqueKey);

            } else if (event.state() == IdleState.WRITER_IDLE) {
                //表示已经多久没有发送数据了

                socketService.getSocketEventService().onHeartNoWriteDataListener(ctx,uniqueKey);

            } else if (event.state() == IdleState.ALL_IDLE) {
                //表示已经多久既没有收到也没有发送数据了

            }
        }
    }
}
