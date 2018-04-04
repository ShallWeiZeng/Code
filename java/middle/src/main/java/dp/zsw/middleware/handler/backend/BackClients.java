package dp.zsw.middleware.handler.backend;

import dp.zsw.middleware.handler.MiddleServer;
import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import dp.zsw.middleware.handler.handler.BackClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dp.zsw.middleware.handler.utils.DataSource;

/**
 * Created by zsw on 2017/10/13.
 *
 */
public class BackClients {

    private static Logger LOG = LoggerFactory.getLogger(BackClients.class);

    private static Bootstrap b = new Bootstrap();
    static {
        //这里线程数可以设置大点
        EventLoopGroup group = new EpollEventLoopGroup(MiddleServer.reflexConfig.getWorkerThreadNum());
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_LINGER, 0)//尽量阻塞到数据发完
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        p.addLast("decode", new ByteArrayDecoder());
                        p.addLast("encode", new ByteArrayEncoder());
                        p.addLast("dp/zsw/middleware/handler",new BackClientHandler());
                    }
                });
    }
    public static Channel startConnection(ChannelHandlerContext ctx, DataSource dataSource){
        try {
            Channel  ch = b.connect(dataSource.getHost(), dataSource.getPort())
                    .addListener((ChannelFutureListener) channelFuture -> {
                        //TODO 当连接上服务器时才会设置初始值
                        if (channelFuture.isSuccess()) {
                            //init registry
                        } else {
                            LOG.error("Client[{}] connected Database Failed,  Cause : {}", ClientConnectionMap.getClientConnection(ctx).getNetId(), channelFuture.cause());
                        }
                    }).sync().channel();
            return ch;
        } catch (InterruptedException e) {
            LOG.warn("start Connection wrong {}", e.getMessage());
        }
        return null;
    }

}