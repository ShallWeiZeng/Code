/**
 * Created by ShangWei on 2017/11/12.
 */
public class SynchronizedDemo {
    /**
     * 一般同步方法
     */
    public synchronized void t1(){
    }
    /**
     * 静态同步方法
     */
    public static synchronized void t2(){
    }
    /**
     * 同步代码块
     */
    public void t3(){
        synchronized (SynchronizedDemo.class){
        }
    }

}
