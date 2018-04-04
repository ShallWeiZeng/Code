package dp.zsw.middleware.handler.backend;

import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import dp.zsw.middleware.handler.utils.DataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsw on 2017/10/14.
 *
 */
public class BackClient {
    private long netId;
    private ChannelHandlerContext serverCtx;
    private ChannelHandlerContext clientCtx;
    private String remoteIP;
    //做唯一标识认证    可以删除 暂时未用上
    private String anotherID;
    private List<Object> msg;
    private boolean fake;
    private Channel channel;
    private DataSource dataSource;
    private boolean authEnd;
    private List<Object> other;
    private boolean md5;
    private boolean changed;


    public BackClient(ChannelHandlerContext ctx, Channel channel, DataSource dataSource) throws Exception{
        this.serverCtx = ctx;
        this.clientCtx = null;
        this.remoteIP = ctx.channel().remoteAddress().toString();
        netId = ClientConnectionMap.getClientConnection(ctx).getNetId();
        this.fake = false;
        this.anotherID = channel.localAddress().toString();
        this.dataSource = dataSource;
        msg = new ArrayList<>();
        other = new ArrayList<>();
        this.authEnd = false;
        this.md5 = false;
    }

    public boolean terminate() throws Exception{
        //关闭链路则可以关闭一个client
        try {
            this.serverCtx.close();
            this.clientCtx.close();
            this.channel.close();
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public List<Object> getMsg() {
        return msg;
    }

    public void add(Object msg){
        this.other.add(msg);
    }

    public void setMsg(Object msg) {
        this.msg.add(msg);
    }

    public String getRemoteIP(){
        return remoteIP;
    }

    public long getNetId() {
        return netId;
    }

    public void setNetId(long netId) {
        this.netId = netId;
    }


    public ChannelHandlerContext getServerCtx() {
        return serverCtx;
    }

    public void setServerCtx(ChannelHandlerContext serverCtx) {
        this.serverCtx = serverCtx;
    }

    public ChannelHandlerContext getClientCtx() {
        return clientCtx;
    }

    public void setClientCtx(ChannelHandlerContext clientCtx) {
        this.clientCtx = clientCtx;
    }

    public void sendMessage(Object msg) throws Exception{
        if (this.clientCtx == null){
            this.channel.writeAndFlush((byte[])msg);
        }
        else
            this.clientCtx.writeAndFlush((byte[])msg);
    }

    public String getAnotherID() {
        return anotherID;
    }

    public void setAnotherID(String anotherID) {
        this.anotherID = anotherID;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isAuthEnd() {
        return authEnd;
    }

    public void setAuthEnd(boolean authEnd) {
        this.authEnd = authEnd;
    }

    public void setMsgList(List<Object> msg){
        this.msg = msg;
    }

    public void flip() {
        this.msg = this.other;
        this.msg.remove(this.msg.size()-1);
        this.other = new ArrayList<>();
    }

    public boolean isMd5() {
        return md5;
    }

    public void setMd5(boolean md5) {
        this.md5 = md5;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
