package dp.zsw.middleware.handler.pgsql;

import dp.zsw.middleware.handler.connection.ClientConnection;
import dp.zsw.middleware.handler.utils.FrontPacket;
import dp.zsw.middleware.handler.utils.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zsw on 2017/11/18.
 *
 */
public class Protocol {
    private static final Logger LOG = LoggerFactory.getLogger(Protocol.class);

    public static String parse(Object msg, ClientConnection cliConn){
        byte[] m = (byte[])msg;
        int len = m.length;
        StringBuilder sb = new StringBuilder();
        if (len <= 0)
            return null;
        if ((char)m[0] == 'P'){
            int index = 0;
            long msgLen = buf4Int(new byte[]{m[index + 1], m[index + 2], m[index + 3], m[index + 4]});
            String res = parseP(m, index + 5, len, cliConn, msgLen);
            if (res != null){
                return res;
            }
            msgLen = Protocol.buf4Int(new byte[]{m[index +1], m[index + 2], m[index + 3], m[index + 4]});
            index += msgLen + 1;
            res = parseMessage(index, m, len, cliConn);
            if (res != null){
                return res;
            }
        }
        else if ((char)m[0] == 'Q'){
            //简单查询
            int index = 5;
            while(index < len && m[index] != 0){
                sb.append((char)(m[index]));
                index++;
            }
            LOG.info("get full sql : " + sb.toString() + "  type: Q");
            return sb.toString();
        } else if ((char)m[0] == 'C'){
            int index = 1;
            int msgLen =(int) buf4Int(new byte[]{m[1], m[2], m[3], m[4]});
            index += msgLen;
            if ((char)m[0] == 'P'){
                String res = parseMessage(index, m, len, cliConn);
                if (res != null){
                    return res;
                }
                return null;
            }
        }
        return null;
    }

    public static long buf4Int(byte[] buf){
        long len = 0;
        len += ((int)(buf[0]&0xff) << 24);
        len += ((int)(buf[1]&0xff) << 16);
        len += ((int)(buf[2]&0xff) << 8);
        len += (int)(buf[3]&0xff);
        return len;
    }

    private static String parseP(byte[] m, int index, int len, ClientConnection cliConn, long msgLen){
        StringBuffer name = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        int start = index - 5;
        while (m[index] != 0) {
            name.append((char)(m[index]));
            index++;
        }
        while (m[index]==0) {
            index++;
        }
        while(index < len && m[index] != 0){
            sb.append((char)(m[index]));
            index++;
        }
        if (name.toString().getBytes().length != 0){
            byte[] packet = new byte[(int)msgLen + 1];
            for (int i = 0, j = start ; i< msgLen + 1; i++, j++)
                packet[i] = m[j];
            FrontPacket frontPacket = new FrontPacket((byte)'P', msgLen + 1, name.toString(), sb.toString() , packet);
            if (sb.toString().equalsIgnoreCase("commit"))
                cliConn.setPackets(frontPacket);
        }
        //check sql
        for (String sqlRegex : RelationParser.sqlRegexs) {
            if (Regex.group1(sb.toString().toUpperCase(), sqlRegex) != null){
                LOG.info("get full sql : " + sb.toString() + "  type: P");
                return sb.toString();
            }
        }
        return null;
    }

    public static String commitName(byte[] o){
        if (o.length <=0){
            return null;
        }
        if (o[0] == 'B'){
            int index = 5;
            StringBuffer name = new StringBuffer();
            StringBuffer sb = new StringBuffer();
            while (o[index] != 0) {
                name.append((char)(o[index]));
                index++;
            }
            while (o[index]==0) {
                index++;
            }
            while(index < o.length && o[index] != 0){
                sb.append((char)(o[index]));
                index++;
            }
            if (sb.toString().getBytes().length!=0)
                return sb.toString();
            return null;
        }
        return null;
    }

    private static String parseMessage(int index, byte[] m, int len, ClientConnection cliConn){
        int current = 0;
        int msgLen = 0;
        String res = null;
        while (index < len){
            if ((char)m[index] == 'S'){
                break;
            }
            else if ((char)m[index] == 'P'){
                msgLen =(int) Protocol.buf4Int(new byte[]{m[index +1], m[index + 2], m[index + 3], m[index + 4]});
                current = index + 5;
                res = parseP(m, current, len, cliConn, msgLen);
                if (res != null){
                    return res;
                }
                index += msgLen + 1;
            }
            else {
                msgLen = (int)Protocol.buf4Int(new byte[]{m[index +1], m[index + 2], m[index + 3], m[index + 4]});
                index += msgLen +1;
            }
        }
        return null;
    }
}
