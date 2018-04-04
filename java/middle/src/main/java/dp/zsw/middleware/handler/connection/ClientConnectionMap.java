package dp.zsw.middleware.handler.connection;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zsw on 2017/10/11.
 *
 */
public class ClientConnectionMap {

    private static Logger LOG = LoggerFactory.getLogger(ClientConnection.class);

    private static ConcurrentHashMap<String, ClientConnection> clientMap  = new ConcurrentHashMap<>();

    public static void addConnection(ChannelHandlerContext ctx){
        ClientConnection conn = new ClientConnection(ctx);

        if (clientMap.putIfAbsent(conn.getRemoteIP(), conn) != null){
            //TODO 决定保存哪一个 两个相同的连接
            LOG.warn("has duplicated remoteIP : {}", conn.getRemoteIP());
            return ;
        }
        LOG.info("add connection success");
    }

    public static synchronized ClientConnection getClientConnection(ChannelHandlerContext ctx) throws Exception{
        String remoteIP = ctx.channel().remoteAddress().toString();
        ClientConnection conn = clientMap.get(remoteIP);
        if (conn != null){
            return conn;
        }
        LOG.warn("ClientConnection  not found in ClientMap remoteIP:{}", remoteIP);
        throw new Exception("ClientConnection not found  ,conn is null : " + remoteIP);
    }

    public static void removeConnection(ChannelHandlerContext ctx) {
        try {
            ClientConnection conn = getClientConnection(ctx);
            clientMap.remove(conn.getRemoteIP());
            LOG.info("client remove netid:{}", conn.getNetId());
        } catch (Exception e) {
            LOG.warn("remove wrong, remoteIP may get null or Conn is null ,check ChannelHandlerContext :{}", ctx.channel().remoteAddress());
        }

    }

    public static boolean isContain(ChannelHandlerContext ctx){
        try {
            return clientMap.containsKey(getClientConnection(ctx).getRemoteIP());
        } catch (Exception e) {
            LOG.warn("contain key may null , current connection may be closed ,reason : {}, ChannelHandlerContext :{}", e.getMessage(), ctx.channel().remoteAddress());
        }
        return false;
    }

}
