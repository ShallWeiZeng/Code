package servers;

import utils.MD5Digest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;

/**
 * Created by zsw on 2017/9/28.
 *
 */
public class BIO2 {
    private Socket socketServer;
    private Socket socketClient;
    private Socket socketClient2;

    private String host;
    private int port;
    private String hostClient;
    private int portClient;
    private String hostClient2;
    private int portClient2;

    private InputStream  sin;
    private OutputStream sout;

    private InputStream  cin;
    private OutputStream cout;
    private InputStream  cin2;
    private OutputStream cout2;


    public BIO2(){
        this.host = "192.168.31.101";
        this.port = 12345;
        this.hostClient = "192.168.31.31";
        this.portClient = 5431;
        this.hostClient2 = "localhost";
        this.portClient2 = 5432;
    }

    public  void runServer(){
        socketServer = null;
        socketClient = null;
        SelectorProvider
        try{
            ServerSocket ss = new ServerSocket(port);
            System.out.println("sever started port:" + port);
            while(true) {
                socketServer = ss.accept();
                runClient();
                boolean flag = true;
                sin = socketServer.getInputStream();
                sout = socketServer.getOutputStream();
                byte[] buf = new byte[2048];
                byte[] buf2;
                byte[] buffer = new byte[2048];
                byte[] buffe2 = new byte[2048];
                int r = -1;
                int i =0;

                while (true) {
                    try {
                        r = sin.read(buf, 0, 2048);
                        if (r < 0) break;
                        System.out.println("i:" + i);
                        buf2 = new String(buf).getBytes();

                        //拦截sql
                        for (int k = 0 ;k < r; k++){
                            System.out.print(buf[k] + ",");
                        }
                        System.out.println();
                        System.out.println(new String(buf));
                        System.out.println("avaliable :" + sin.available());
                        //client 连接断开
                        if (new String(buf).substring(0,1).equalsIgnoreCase("X")) {
                            closeClient();
                            break;
                        }
                        write(buf, r, cout, false);
                        if (buffe2[0] == 82 && flag) {
                            write(buffe2, r, cout2, true);
                            flag = false;
                        }
                        else
                            write(buf2, r, cout2, false);

                        r = cin2.read(buffe2, 0, 2048);
                        System.out.println("1:" + new String(buffe2));
                        r = cin.read(buffer, 0, 2048);
                        System.out.println("2:" + new String(buffer));
                        if(r != -1 ){
                            for (int j = 0;j<r;j++)
                                sout.write(buffer[j]);
                        }
                        sout.flush();

                        buf = new byte[2048];
                        i = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void runClient(){
        socketClient = null;
        socketClient2 = null;
        try {
            socketClient = new Socket();
            socketClient2 = new Socket();
            socketClient.connect(new InetSocketAddress(hostClient, portClient));
            socketClient2.connect(new InetSocketAddress(hostClient2, portClient2));

            cin = socketClient.getInputStream();
            cout = new BufferedOutputStream(socketClient.getOutputStream(),8196);
            cin2 = socketClient2.getInputStream();
            cout2 = new BufferedOutputStream(socketClient2.getOutputStream(), 8196);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] buf, int r, OutputStream cout, boolean fake){
        try {
            if (fake){
                cout.write('p');
                byte[] salt = {buf[9], buf[10], buf[11], buf[12]};
                byte[] digest = MD5Digest.encode("shenjianshou".getBytes("UTF-8"), "mnbvcxz@123".getBytes("UTF-8"), salt);
                int val = 4 + digest.length + 1;
                cout.write((byte)(val >>> 24));
                cout.write((byte)(val >>> 16));
                cout.write((byte)(val >>> 8));
                cout.write((byte)(val));
                cout.write(digest);
                cout.write(0);
                cout.flush();
                return;
            }
            for (int i =0;i< r ;i++) {
                cout.write(buf[i]);
            }
            cout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean closeClient(){
        try {
            socketClient.close();
            socketClient2.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void encodePS(){

    }


    public static void main(String[] args){
        BIO2 server = new BIO2();
        server.runServer();

    }

}
