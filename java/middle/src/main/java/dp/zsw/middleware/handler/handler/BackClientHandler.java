package dp.zsw.middleware.handler.handler;


import dp.zsw.middleware.handler.backend.BackClient;
import dp.zsw.middleware.handler.backend.BackClientMap;
import dp.zsw.middleware.handler.connection.ClientConnection;
import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import dp.zsw.middleware.handler.utils.MD5Digest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zsw on 2017/10/14.
 *
 */
public class BackClientHandler  extends ChannelInboundHandlerAdapter {
    private static Logger LOG = LoggerFactory.getLogger(BackClientHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //DO NOTHING
        LOG.info("a BackClient has built success, database has connected, Client ctx : {}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long netId = -1;
        try {
            BackClient backClient = BackClientMap.getBackClient(ctx.channel());
            if (backClient != null) {
                byte[] m =  (byte[]) msg;
                ClientConnection cliConn = ClientConnectionMap.getClientConnection(backClient.getServerCtx());
                if (cliConn != null && backClient.isChanged() && cliConn.isUsed() && m[0] == 49) {
                    byte[] newByte = new byte[m.length -5];
                    System.arraycopy(m, 5,newByte,0, m.length-5);
                    msg = newByte;
                    backClient.setChanged(false);
                }
                netId = backClient.getNetId();
            }
            receiveMessage(ctx, msg);
        } catch (Exception e) {
            //TODO 会有空等待问题 暂时直接关闭
            LOG.warn("send Message failed  exception : {}, netId : {} Client ctx : {}", e, netId, ctx );
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //3种断开连接的情况，1.主动；2.服务器连接数不够关闭了这边的一个连接；3.未知的情况
        LOG.info("Client ctx : {} , this client is inActive", ctx.channel());
        BackClient backClient = null;
        try {
            backClient = BackClientMap.getBackClient(ctx.channel());
            if (backClient != null) {
                backClient.terminate();
            }
        } catch (Exception e) {
            LOG.warn("exception ： {} , ctx : {}", e, ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn("Client ctx : {}, cause exception : {} ", ctx, cause.getMessage() );
        ctx.close();
    }

    /**
     * 决定哪一个报文需要返回回去，哪一个报文需要手动加密
     * @param ctx client
     */
    private void receiveMessage(ChannelHandlerContext ctx, Object msg) throws Exception{
        BackClient backClient = BackClientMap.getBackClient(ctx.channel());
        //如果被选中了，那么就是这个client返回回去数据
        if (backClient == null){
            throw  new Exception("something happened ,this client should not be closed, please check");
        }
        if (backClient.getClientCtx() == null){
            backClient.setClientCtx(ctx);
        }
        ClientConnection conn = ClientConnectionMap.getClientConnection(backClient.getServerCtx());
        if (backClient.isFake()){
            //do auth
            if (backClient.getMsg().size() > 0) {
                Object message = backClient.getMsg().get(0);
                backClient.add(message);
                backClient.getMsg().remove(0);
                byte[] buf = (byte[]) msg;
                // V3 version(7.5+) ->  0 : R , 1-4 : len, 5-8: type = (buf[5] & 0xFF) << 24 | (buf[6] & 0xFF) << 16 | (buf[7] & 0xFF) << 8 | buf[8]& 0xFF
                if (buf.length > 8){
                    //这里无须担心客户端会发送too many clients ,认证部分不会出现这个情况 但以防万一
                    if (new String(buf).contains("too many clients")) {
                        conn.sendMessage(msg);
                        //直接断开client和server端，这个时候连接数已经超限
                        backClient.terminate();
                        LOG.warn("database reach to the MAX_CONNECTION dataSource : {} msg : {}, byte : {}", backClient.getDataSource().toString(), new String((byte[])msg), msg);
                        return;
                    }
                }
                if (buf[0] == 'E'){
                    conn.sendMessage(msg);
                    backClient.terminate();
                    LOG.warn("unexpated error msg : {}", new String((byte[])msg));
                    return;
                }
                if (new String((byte[])message).contains("md5") && !backClient.isMd5()){
                    byte[] salt = {buf[9], buf[10], buf[11], buf[12]};
                    byte[] digest = MD5Digest.encode(backClient.getDataSource().getUsername().getBytes("UTF-8"), backClient.getDataSource().getPassword().getBytes("UTF-8"), salt);
                    int val = 4 + digest.length + 1;
                    byte[] cout = new byte[val + 1];
                    cout[0] = 'p';
                    cout[1] = (byte)(val >>> 24);
                    cout[2] = (byte)(val >>> 16);
                    cout[3] = (byte)(val >>> 8);
                    cout[4] = (byte)(val);
                    System.arraycopy(digest,0, cout, 5, digest.length);
                    cout[val] = 0;
                    message = cout;
                    backClient.setMd5(true);
                }
                LOG.info("netId : {} , channel : {}, msg : {}, original msg : {}", backClient.getNetId(), backClient.getChannel(), new String((byte[])message), new String((byte[])msg));

                backClient.sendMessage(message);
                return;
            }
            backClient.setFake(false);
            backClient.setAuthEnd(true);
            backClient.flip();
        }
        //唯一返回
        conn.sendMessage(msg);
    }
}
