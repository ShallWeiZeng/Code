package dp.zsw.middleware.handler.handler;

import dp.zsw.middleware.handler.MiddleServer;
import dp.zsw.middleware.handler.backend.BackClient;
import dp.zsw.middleware.handler.backend.BackClientMap;
import dp.zsw.middleware.handler.backend.BackClients;
import dp.zsw.middleware.handler.connection.ClientConnection;
import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import dp.zsw.middleware.handler.route.RouteService;
import dp.zsw.middleware.handler.utils.DataSource;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by zsw on 2017/10/11.
 *
 */
public class MiddleServerHandler extends ChannelInboundHandlerAdapter {
    private static Logger LOG = LoggerFactory.getLogger(MiddleServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            LOG.info("chanel Read ctx : " + ctx.channel() + "  msg:" + new String((byte[])msg));
            ClientConnection conn = ClientConnectionMap.getClientConnection(ctx);
            conn.addMsg(msg);
            conn.sendMessage("Server recieve".getBytes());
        } catch (Exception e) {
            // 承接上一个错误
            LOG.warn(e.getMessage());
            closeClient(ctx);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Remote address has connected :" + ctx.channel().remoteAddress() + " active" );
        //add ClientConnection and start a  connection (not a BackClient)
        System.out.println(Thread.currentThread().getStackTrace());
        ClientConnectionMap.addConnection(ctx);
//        //TODO  修改方式 先只连接一个
//        //连接默认的那个
//        for (DataSource dataSource : MiddleServer.reflexConfig.getDatabases()){
//            if (!dataSource.isDef()) continue;
//            Channel channel = BackClients.startConnection(ctx, dataSource);
//            if (channel == null){
//                //terminated
//                ctx.close();
//                return;
//            }
//            try {
//                BackClient backClient = new BackClient(ctx, channel, dataSource);
//                backClient.setChannel(channel);
//                BackClientMap.addBackClient(ClientConnectionMap.getClientConnection(ctx).getNetId(), backClient);
//                BackClientMap.addBackClient(channel, backClient);
//                break;
//            } catch (Exception e) {
//                LOG.warn("add BackClient may failed , reason : {}", e.getMessage());
//            }
//        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //TODO 这里只有两种模式，则是那边主动断开，需要添加另外一种情况
        LOG.info("Remote address has disconnected :" + ctx.channel().remoteAddress() + " inactive");
        //terminate BackThread in case client unexpected disconnect
//        closeClient(ctx);
        //remove Backclient

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn("exception : {}, ctx ; {}", cause.getMessage(), ctx);
        closeClient(ctx);
    }

    //TODO 为第二个连接做判断 交由路由服务来操作
    private void sendMessage(ChannelHandlerContext ctx, Object msg){
        try {
            RouteService.sendMessage2(ctx, msg);
        } catch (Exception e) {
            //TODO 这里会出现问题 会有空等待的情况 暂时直接断开
            LOG.warn("exception : {} , Server ctx : {}, ", e.getStackTrace(), ctx );
//            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            ClientConnection conn = ClientConnectionMap.getClientConnection(ctx);
//            int len = 0;
//            for (Object o : conn.getMsg()) {
//                len += ((byte[])o).length;
//            }
//
//            byte[] m = new byte[len];
//            int i = 0;
//            for (Object o : conn.getMsg()) {
//                len = ((byte[])o).length;
//                for (int j = 0; j < len; j++, i++) {
//                    m[i] = ((byte[])o)[j];
//                }
//            }
//            conn.flip();
//            if (m.length < 300)
//                LOG.info("Middle server_receive msg netId : " + ClientConnectionMap.getClientConnection(ctx).getNetId() + " ctx : " + ctx + " msg : " + new String(m));
//            else
//                LOG.info("Middle server receive msg netId : " + ClientConnectionMap.getClientConnection(ctx).getNetId() + " ctx : " + ctx + " msg : too large to show" );
//            //交由netty client去发送
//            sendMessage(ctx, m);
        } catch (Exception e) {
            LOG.warn("exception : {}", e.getMessage());
//            closeClient(ctx);
        }
    }

    /**
     *在出现异常的时候进行关闭连接，并移出map
     * @param ctx server
     */
    private static void closeClient(ChannelHandlerContext ctx){
        BackClient backClient = null;
        if (BackClientMap.isContains(ctx)){
            try {
                backClient = BackClientMap.getBackClient(ctx);
                if (backClient  != null){
                    BackClientMap.removeBackClient(backClient.getClientCtx().channel());
                    BackClientMap.removeBackClient(ctx);
                    backClient.terminate();
                    LOG.info("netId : {} ctx : {} , backclient close success", backClient.getNetId(), ctx);
                }
                else {
                    LOG.warn("terminate backClient failed , current backClient is null , ctx : {}", ctx);
                }
            } catch (Exception e) {
                if (backClient != null)
                    LOG.warn("netId : {} ctx : {} , backclient close failed ,reason {}", backClient.getNetId(), ctx, e.getMessage());
                else
                    LOG.warn("backClient is null, backClient close failed , reason {}", e.getCause());
            }
        }
        //remove ClientConnection
        if (ClientConnectionMap.isContain(ctx)){
            ClientConnectionMap.removeConnection(ctx);
        }
    }


}
