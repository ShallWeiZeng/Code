package dp.zsw.middleware.handler;

import dp.zsw.middleware.handler.handler.MiddleServerHandler;
import dp.zsw.middleware.handler.utils.JedisPoolsUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import dp.zsw.middleware.handler.route.RouteService;
import sun.misc.Signal;

import java.net.InetSocketAddress;
import java.util.TimeZone;

/**
* Created by zsw on 2017/10/11.
*
*/
public class MiddleServer {
    private static final Logger LOG = LoggerFactory.getLogger(MiddleServer.class);
    private static final String DEFAULT_CONF_FILE = "conf/database.xml";

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("PRC"));
    }

    public static ReflexConfig reflexConfig;

    public static void main(String[] args){
        String conf = DEFAULT_CONF_FILE;
        try {
//            reflexConfig = new ReflexConfig(conf);
//            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//            jedisPoolConfig.setMaxWaitMillis(5000L);
//            jedisPoolConfig.setMaxTotal(reflexConfig.getRedisMax());
//            JedisPoolsUtils.init(reflexConfig.getMasterName(),reflexConfig.getSentinels(), jedisPoolConfig);
//            Signal.handle(new Signal("USR2"), signal -> {
//                LOG.info("receive signal USR2, clear mapping cache");
//                RouteService.clearCache();
//                LOG.info("clear mapping cache finish");
//            });
        } catch (Exception e) {
            LOG.warn("init failed {}", e.getMessage());
            System.exit(-1);
        }

        MiddleServer.startMiddleServer(9875);
    }

    private static void startMiddleServer(int port){
//        EventLoopGroup testGroup = new EpollEventLoopGroup();
//        Channel
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("encode", new ByteArrayEncoder());
                        pipeline.addLast("decode", new ByteArrayDecoder());
                        pipeline.addLast("handler", new MiddleServerHandler());
                    }
                });
        bindConnectionOptions(bootstrap);

        try {
            ChannelFuture f = bootstrap.bind(new InetSocketAddress(port))
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()){
                            //TODO 初始化 hashmap
                            LOG.info("MidderServer Started Success");
                        }
                        else {
                            LOG.error("MidderServer Started failed  cause : " + future.cause());
                        }
                    }
                    ).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.warn("server shut down : " + e.getMessage());
        } finally {
            LOG.info("MidderServer Shutdown");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private static void bindConnectionOptions(ServerBootstrap bootstrap) {

//        bootstrap.option(ChannelOption.SO_BACKLOG, MiddleServer.reflexConfig.getMaxClient());//SO_BACKLOG 对应的是tcp/ip的listen函数的backlog参数，backlog参数指定了队列的大小
        bootstrap.option(ChannelOption.SO_BACKLOG, 5000);//SO_BACKLOG 对应的是tcp/ip的listen函数的backlog参数，backlog参数指定了队列的大小

        bootstrap.childOption(ChannelOption.SO_LINGER, 0);//尽量阻塞到数据完全发送
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

    //        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //心跳机制使用TCP选项
    }
}
