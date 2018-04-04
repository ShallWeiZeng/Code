import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ShangWei on 2017/12/11.
 */
public class TestServer {

    @Test
    public void test(){
        for (int i=0 ;i< 1; i++) {
//            new Thread(()->{
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(9875));
                    OutputStream cout  = new BufferedOutputStream(socket.getOutputStream(),8196);
                    cout.write("Success".getBytes());
                    cout.flush();
                    InputStream sin = socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(sin));
                    String info = "";
                    while((info=br.readLine())!=null){
                        System.out.println("我是客户端，服务器说："+info);
                    }
                    socket.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
//            }).start();
        }

    }

    @Test
    public void testDruid(){
        int cap = 5;
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        int MAXIMUM_CAPACITY = 1 << 30;
        System.out.println((n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1);
    }

    @Test
    public void testRWL(){
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        int o =3;
        try {
            lock.readLock().lock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        try {
            lock.readLock().lock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        Condition condition = lock.writeLock().newCondition();

        condition.signal();
        condition.signalAll();
        try {
            lock.readLock().lock();
            condition.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
    }
}
