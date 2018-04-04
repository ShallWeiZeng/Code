package dp.zsw.middleware.handler.connection;

import dp.zsw.middleware.handler.utils.FrontPacket;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zsw on 2017/10/11.
 *
 */
public class ClientConnection {
    private static final AtomicLong netidGenerator = new AtomicLong(0);
    private long netId;
    private String remoteIP;
    private ChannelHandlerContext ctx;
    private List<Object> msg;
    private FrontPacket packets;
    private boolean isUsed;

    ClientConnection(ChannelHandlerContext c){
        remoteIP  = c.channel().remoteAddress().toString();
        //最大为0x7fffffffffffffffL, 不知道会不会爆掉，如果爆掉则是从负的开始
        netId = netidGenerator.incrementAndGet();
        ctx = c;
        msg = new ArrayList<>();
        isUsed = false;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public long getNetId() {
        return netId;
    }

    public void setNetId(long netId) {
        this.netId = netId;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    //发送数据回真正的client端
    public void sendMessage(Object msg) throws Exception{
        this.ctx.writeAndFlush(msg);
    }

    public void flip(){
        this.msg.clear();
    }

    public List<Object> getMsg() {
        return msg;
    }

    public void addMsg(Object msg) {
        this.msg.add(msg);
    }

    public FrontPacket getPackets() {
        return packets;
    }

    public void setPackets(FrontPacket packets) {
        this.packets = packets;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
