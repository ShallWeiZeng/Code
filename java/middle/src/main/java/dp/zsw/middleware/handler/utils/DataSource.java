package dp.zsw.middleware.handler.utils;

/**
 * Created by MorningStar on 2017/11/26.
 * 使用反射读取文件
 */
public class DataSource implements Comparable<DataSource> {
    private String host;
    private int port;
    private String username;
    private String password;
    private String name;
    private boolean def;
    private boolean createDefault;

    public DataSource(){}
    public DataSource(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSource that = (DataSource) o;

        if (port != that.port) return false;
        return host != null ? host.equals(that.host) : that.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public int compareTo(DataSource o) {
        return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        return "DataSource{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", def=" + def +
                ", createDeafult=" + createDefault +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDef() {
        return def;
    }

    public void setDef(boolean def) {
        this.def = def;
    }

    public boolean isCreateDefault() {
        return createDefault;
    }

    public void setCreateDefault(boolean createDefault) {
        this.createDefault = createDefault;
    }
}
