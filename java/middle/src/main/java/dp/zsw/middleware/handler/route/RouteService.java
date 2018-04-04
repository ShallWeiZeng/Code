package dp.zsw.middleware.handler.route;

import dp.zsw.middleware.handler.MiddleServer;
import dp.zsw.middleware.handler.backend.BackClient;
import dp.zsw.middleware.handler.backend.BackClientMap;
import dp.zsw.middleware.handler.backend.BackClients;
import dp.zsw.middleware.handler.connection.ClientConnection;
import dp.zsw.middleware.handler.connection.ClientConnectionMap;
import dp.zsw.middleware.handler.utils.FrontPacket;
import dp.zsw.middleware.handler.utils.JedisPoolsUtils;
import dp.zsw.middleware.handler.utils.Regex;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dp.zsw.middleware.handler.pgsql.Protocol;
import dp.zsw.middleware.handler.pgsql.RelationParser;
//import redis.clients.jedis.Jedis;
import dp.zsw.middleware.handler.utils.DataSource;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zsw on 2017/10/18.
 *
 */
public class RouteService {
    private static final Logger LOG = LoggerFactory.getLogger(RouteService.class);

    private static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Map<String, String> cache = new HashMap<>();


    /**
     * 发送对应报文到对应地点
     * @param ctx 来自server端
     * @param msg msg
     */
    public static void sendMessage2(ChannelHandlerContext ctx, Object msg) throws Exception{
        DataSource dataSource = null;
        //show debug 来调试用
        byte[] show = (byte[])msg;
        String debug = new String(show);
        LOG.info("ctx " + ctx + "  show : {}  show str :" + debug, show);
        BackClient backClient = BackClientMap.getBackClient(ctx);
        if (backClient == null){
            throw new Exception("current ctx could not found backClient ctx : " + ctx );
        }
        ClientConnection clientConnection = ClientConnectionMap.getClientConnection(ctx);
        if (clientConnection == null){
            throw new Exception("current ctx could not found clientConnection  ctx :" + ctx);
        }
        String sql = Protocol.parse(msg, clientConnection);
        if (sql != null){
            sql = sql.toUpperCase();
            String tableName = RelationParser.parseTable(sql);
            LOG.info("sql : {} ,   tableName : {},  channel : {}" , sql, tableName, backClient.getChannel());
            boolean flag = false;
            String res = null;
            if (tableName != null && !tableName.equalsIgnoreCase("")){
                String[] tables = tableName.split(",");
                //check table name;
                for (String table : tables) {
                    for (String regex : RelationParser.regexs){
                        res = Regex.group1(table.toUpperCase(), regex);
                        if (res != null) {
                            break;
                        }
                    }
                    if (res == null){
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag && tableName != null && !tableName.equalsIgnoreCase("")) {
                //TODO 进来的条件是有表名 TOCHECK
                String[] tables = tableName.split(",");
                Jedis jedis = null;
                try {
                    jedis = JedisPoolsUtils.getJedis();
                    String serverName;
                    Set<String> servers = new HashSet<>();
                    for (String table : tables) {
                        try {
                            readWriteLock.readLock().lock();
                            serverName = cache.get(table.toUpperCase());
                        } finally {
                            readWriteLock.readLock().unlock();
                        }
                        if (serverName == null || serverName .length() == 0) {
                            serverName = jedis.hget(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase());
                            if (serverName != null){
                                addCache(table, serverName);
                            }
                        }
                        if (serverName != null){
                            servers.add(serverName);
                        }
                    }
                    //TODO CHECK 没有并发的访问,不会存在竞争，但是是否会影响性能
                    int size = servers.size();
                    if (size == 0){
                        //都没有包含对应关系
                        if (tables.length > 1){
                            //默认库 并添加上对应关系
                            dataSource = MiddleServer.reflexConfig.getDefaultDataSource();
                            for (String table : tables) {
                                jedis.hset(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase(), dataSource.getName());
                                addCache(table, dataSource.getName());
                            }
                        }
                        else{
                            //单表
                            if (isCreate(sql)){
                                //新库同时设置对应关系
                                for (DataSource source : MiddleServer.reflexConfig.getDatabases()) {
                                    if (source.isCreateDefault()){
                                        dataSource = source;
                                        break;
                                    }
                                }
                                for (String table : tables) {
                                    if (dataSource != null) {
                                        jedis.hset(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase(), dataSource.getName());
                                        addCache(table, dataSource.getName());
                                    }
                                }
                            }
                            //走默认
                            else {
                                dataSource = MiddleServer.reflexConfig.getDefaultDataSource();
                                for (String table : tables) {
                                    jedis.hset(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase(), dataSource.getName());
                                    addCache(table, dataSource.getName());
                                }
                            }
                        }
                    }
                    //含有库且唯一
                    else if (size == 1){
                        //是否是多张表 TO CHECK
                        if (isCreate(sql)){
                            //新库同时设置对应关系, 不是视图
                            for (DataSource source : MiddleServer.reflexConfig.getDatabases()) {
                                if (source.isCreateDefault()){
                                    dataSource = source;
                                    break;
                                }
                            }
                            for (String table : tables) {
                                jedis.hset(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase(), dataSource.getName());
                                addCache(table, dataSource.getName());
                            }
                        }
                        //多表直接走该库
                        else {
                            for (DataSource source : MiddleServer.reflexConfig.getDatabases()) {
                                if (servers.contains(source.getName())) {
                                    dataSource = source;
                                    break;
                                }
                            }
                            //当有的对应关系为空的时候会设置
                            if (dataSource != null) {
                                String dbName = null;
                                for (String table : tables) {
                                    try {
                                        readWriteLock.readLock().lock();
                                        dbName = cache.get(table.toUpperCase());
                                    } finally {
                                        readWriteLock.readLock().unlock();
                                    }
                                    if (dbName == null || dbName.length() == 0) {
                                        jedis.hset(MiddleServer.reflexConfig.getTableMap(), table.toUpperCase(), dataSource.getName());
                                        addCache(table, dataSource.getName());
                                    }
                                }
                            }
                        }
                    }
                    else {
                        throw new Exception("这里代表着，该条sql语句中有库但是各个库都不一样，出现了非正常情况 sql:" + sql);
                    }
                } catch (Exception e){
                    LOG.warn("redis query failed : {}", e.getMessage());
                    throw e;
                } finally {
                    if (jedis != null)
                        jedis.close();
                }
            }
        }
        if (dataSource != null) {
            if (!backClient.getDataSource().equals(dataSource)){
                connectAnother(backClient, msg, dataSource, ctx);
                return;
            }
            //恰好是当前的datasource
            try{
                backClient.setAuthEnd(true);
                send(backClient, msg, clientConnection, show);
            } catch (Exception e){
                LOG.warn("send msg failed : {}", e.getMessage());
            }
        }
        else {
            //直接发送数据
            backClient = BackClientMap.getBackClient(ctx);
            try {
                send(backClient, msg, clientConnection, show);
            } catch (Exception e){
                //ignore
            }
        }
    }

    private static void connectAnother(BackClient backClient, Object msg, DataSource dataSource, ChannelHandlerContext ctx){
        //todo 需要移除并更换client（check）
        backClient.setMsg(msg);
        BackClientMap.removeBackClient(ctx);
        //开一个新的连接
        try {
            Channel channel = BackClients.startConnection(ctx, dataSource);
            if (channel == null){
                backClient.terminate();
                throw new Exception("channel is null , the database config may be wrong,or the database has shutdown");
            }
            LOG.info("transfer to another database channel: {}, ctx : {}", channel, backClient.getClientCtx());
            BackClient newClient = new BackClient(ctx, channel,dataSource);
            newClient.setMsgList(backClient.getMsg());
            newClient.setChannel(channel);
            BackClientMap.addBackClient(ClientConnectionMap.getClientConnection(ctx).getNetId(), newClient);
            BackClientMap.addBackClient(channel, newClient);
            BackClientMap.removeBackClient(backClient.getChannel());
            //下面不能随意更换位置
            backClient.getClientCtx().close();
            newClient.add(newClient.getMsg().get(0));
            newClient.setFake(true);
            LOG.info("send msg : {} channel : {}", new String((byte[])newClient.getMsg().get(0)), channel);
            Object message = newClient.getMsg().get(0);
            newClient.getMsg().remove(0);
            newClient.setChanged(true);
            ClientConnection cliConn = ClientConnectionMap.getClientConnection(ctx);
            cliConn.setUsed(false);
            newClient.sendMessage(message);
        } catch (Exception e) {
            LOG.warn("create another client failed , reason : {} ", e);
        }
    }

    private static void send(BackClient backClient, Object msg, ClientConnection cliConn, byte[] show ) throws Exception {
        if (backClient != null){
            FrontPacket packet = cliConn.getPackets();
            if (packet != null && backClient.isChanged() && packet.getName().equalsIgnoreCase(Protocol.commitName(show))) {
                byte[] front =packet.getOrigin();
                byte[] newMsg = new byte[front.length + show.length];
                int idx = 0;
                for (int i = 0; i < front.length; idx++, i++) {
                    newMsg[idx] = front[i];
                }
                for (int i = 0; i < show.length; idx++, i++) {
                    newMsg[idx] = show[i];
                }
                msg = newMsg;
                cliConn.setUsed(true);
            }
            backClient.sendMessage(msg);
            if (!backClient.isAuthEnd()){
                if (new String((byte[])msg).contains("md5")){
                    backClient.setAuthEnd(true);
                }
                backClient.setMsg(msg);
            }
        }
    }

    private static boolean isCreate(String sql) {
        String res = Regex.group1(sql.toUpperCase(), "^CREATE\\s+TABLE\\s+.*(TASK_[^\\s\\.\"\']+)");
        return res != null && !res.equalsIgnoreCase("");
    }

    private static void addCache(String key, String value){
        try {
            readWriteLock.writeLock().lock();
            cache.put(key.toUpperCase(), value);
        } catch (Exception e) {
            LOG.warn("{}", e.getMessage());
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public static void clearCache(){
        try {
            readWriteLock.writeLock().lock();
            LOG.info("cache size : {}", cache.size());
            cache.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
