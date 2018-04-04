package dp.zsw.middleware.handler;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dp.zsw.middleware.handler.utils.DataSource;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by MorningStar on 2017/11/26.
 *
 */
public class ReflexConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ReflexConfig.class);

    private String middleServer;
    private int listen;

    private int bossThreadNum;
    private int workerThreadNum;

    private Set<DataSource> databases;
    private DataSource defaultDataSource;
    private String defaultServer;
    private int maxClient;
    private int DBCount;
    //redis
    private String server;
    private int port;
    private String masterName;
    private Set<String> sentinels;
    private int redisMax;
    private String tableMap;

    public ReflexConfig(String conf) throws Exception{
        readXML(conf);
        for (DataSource source : getDatabases()) {
            if (!source.isCreateDefault()){
                this.defaultDataSource = source;
                this.defaultServer = source.getName();
                break;
            }
        }
    }

    private  void readXML(String XMLPath) throws Exception{
        File file = new File(XMLPath);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root =  doc.getRootElement();
        Field[] properties = this.getClass().getDeclaredFields();
        Method setMethod;
        Element foo;

        Set<DataSource> dataSources = new TreeSet<>();

        for (Iterator i  =  root.elementIterator(); i.hasNext();){
            foo = (Element) i.next();
            if (foo.getName().equalsIgnoreCase("dp/zsw/middleware/handler/pgsql")){
                for (Iterator j  = foo.elementIterator(); j.hasNext();){
                    Element node = (Element)j.next();
                    DataSource dataSource = new DataSource();
                    Field[] prop = dataSource.getClass().getDeclaredFields();
                    for (Field field : prop) {
                        setMethod = dataSource.getClass().getMethod("set" + field.getName().substring(0, 1).toUpperCase()
                                + field.getName().substring(1), field.getType());
                        if (setMethod.getParameterTypes().length > 0){
                            if (setMethod.getParameterTypes()[0].getName().equalsIgnoreCase("int")){
                                setMethod.invoke(dataSource, Integer.parseInt(node.elementText(field.getName())));
                            }
                            else if (setMethod.getParameterTypes()[0].getName().equalsIgnoreCase("boolean")){
                                setMethod.invoke(dataSource, Boolean.parseBoolean(node.elementText(field.getName())));
                            }
                            else
                                setMethod.invoke(dataSource, node.elementText(field.getName()));
                        }
                    }
                    LOG.info("dataSource : " + dataSource.toString());
                    dataSources.add(dataSource);
                    this.DBCount++;
                }
                this.databases = dataSources;
            }
            else {
                for (Field field : properties) {
                    if (field.getName().equalsIgnoreCase(foo.getName())){
                        setMethod = this.getClass().getMethod("set" + field.getName().substring(0, 1).toUpperCase()
                                + field.getName().substring(1), field.getType());

                        if (setMethod.getParameterTypes().length > 0) {
                            if (foo.getName().equalsIgnoreCase("sentinels")){
                                Set<String> sentinels = new HashSet<>();
                                String[] sentinel = foo.getTextTrim().split(",");
                                for (String s : sentinel) {
                                    if (!s.matches("\\d+\\.\\d+.\\d+\\.+\\d+:\\d+")) {
                                        throw new Exception("invalid sentinel " + s);
                                    }
                                    sentinels.add(s);
                                }
                                this.sentinels = sentinels;
                            }
                            else if (setMethod.getParameterTypes()[0].getName().equalsIgnoreCase("int")) {
                                setMethod.invoke(this, Integer.parseInt(foo.getTextTrim()));
                            } else
                                setMethod.invoke(this, foo.getTextTrim());
                            LOG.info(field.getName() + ":" + foo.getTextTrim());
                            break;
                        }
                    }
                }
            }
        }
    }

    public String getMiddleServer() {
        return middleServer;
    }

    public void setMiddleServer(String middleServer) {
        this.middleServer = middleServer;
    }

    public int getListen() {
        return listen;
    }

    public void setListen(int listen) {
        this.listen = listen;
    }

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getWorkerThreadNum() {
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public int getMaxClient() {
        return maxClient;
    }

    public void setMaxClient(int maxClient) {
        this.maxClient = maxClient;
    }

    public Set<DataSource> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<DataSource> databases) {
        this.databases = databases;
    }

    public int getDBCount(){
        return this.DBCount;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public Set<String> getSentinels() {
        return sentinels;
    }

    public void setSentinels(Set<String> sentinels) {
        this.sentinels = sentinels;
    }

    public int getRedisMax() {
        return redisMax;
    }

    public void setRedisMax(int redisMax) {
        this.redisMax = redisMax;
    }

    public String getTableMap() {
        return tableMap;
    }

    public void setTableMap(String tableMap) {
        this.tableMap = tableMap;
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public String getDefaultServer() {
        return defaultServer;
    }
}
