package dp.zsw.middleware.handler.backend;


import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zsw on 2017/10/11.
 *
 */
public class BackClientMap {
    private static Logger LOG = LoggerFactory.getLogger(BackClientMap.class);

    private static ConcurrentHashMap<Long, BackClient> clientMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Channel, BackClient> channelMap = new ConcurrentHashMap<>();

    public static void removeBackClient(ChannelHandlerContext ctx){
        Long netId = (long) -1;
        try {
            netId = ClientConnectionMap.getClientConnection(ctx).getNetId();
            clientMap.remove(netId);
            LOG.info("netId : {} ChannelHandlerContext : {} remove client success", netId, ctx);
        } catch (Exception e) {
            LOG.warn("netId : {} ChannelHandlerContext : {}, exception : {}", netId, ctx, e.getMessage());
        }
    }

    public static void removeBackClient(Channel channel){
        try{
            channelMap.remove(channel);
        } catch (Exception e){
            LOG.warn("remove channel failed : {}, channel : {}", e.getMessage(), channel);
        }
    }

    public static void addBackClient(Long id , BackClient backClient){
        if (clientMap.putIfAbsent(id, backClient) != null){
            LOG.info("netId : {} Server ctx : {}", id, backClient.getServerCtx());
            return ;
        }
        //为防止并发，先直接设置只由这个发送回去

        LOG.info("netId : {} Server ctx : {}", id, backClient.getServerCtx());
    }

    public static void addBackClient(Channel channel, BackClient backClient){
        if (channelMap.putIfAbsent(channel, backClient) != null){
            LOG.warn("channel : {} put client failed, Duplicated channel {}", channel, channelMap.containsKey(channel));
            return;
        }
        LOG.info("add backClient success, channel : {} Server ctx : {}", channel, backClient.getServerCtx());
    }

    public static BackClient getBackClient(ChannelHandlerContext ctx) {
        Long netId= (long ) -1;
        try {
            netId = ClientConnectionMap.getClientConnection(ctx).getNetId();
            return clientMap.get(netId);
        } catch (Exception e) {
            LOG.warn("netId :{} ChannelhandlerContext : {} , clientMap return null  reason :{}", netId, ctx, e.getMessage());
        }
        return null;
    }



    public static BackClient getBackClient(Channel channel){
        try {
            if (channel != null){
                return channelMap.get(channel);
            }
        } catch (Exception e) {
            LOG.warn("get channel failed : {} , channel : {}", e.getMessage(), channel);
        }
        return null;
    }


    //TODO 需要判断两个都关闭了
    public static boolean isContains(ChannelHandlerContext ctx) {
        long netId = (long)-1;
        try {
            netId = ClientConnectionMap.getClientConnection(ctx).getNetId();
            return clientMap.containsKey(netId);
        } catch (Exception e){
            LOG.warn("netId : {} ChannelHandlerContext : {} ; exception : {}", netId, ctx, e.getMessage());
        }
        return false;
    }


}