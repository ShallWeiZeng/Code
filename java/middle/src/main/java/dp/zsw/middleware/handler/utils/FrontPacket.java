package dp.zsw.middleware.handler.utils;

/**
 * Created by MorningStar on 2017/11/22.
 *
 */
public class FrontPacket {
    private byte head;
    private long length;
    private String name;
    private String prepare;
    private byte[] origin;

    /**
     * TODO 自解析
     * @param o packet
     */
    public FrontPacket (Object o){
    }

    public FrontPacket (byte head, long length, String name, String prepare, byte[] packet){
        this.head = head;
        this.length = length;
        this.name = name;
        this.prepare = prepare;
        this.origin = packet;
    }

    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrepare() {
        return prepare;
    }

    public void setPrepare(String prepare) {
        this.prepare = prepare;
    }

    public byte[] getOrigin() {
        return origin;
    }

    public void setOrigin(byte[] origin) {
        this.origin = origin;
    }
}
