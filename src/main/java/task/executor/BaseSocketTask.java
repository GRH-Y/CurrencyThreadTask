package task.executor;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * socket 网络通信任务
 * 
 * Created by No.9 on 2018/2/18.
 *
 * @author yyz
 */

public class BaseSocketTask<D> extends BaseConsumerTask<D> {


    protected void onConnectSuccess() {
        //Do something
    }

    /**
     * 每次socket建立链接失败后回调一次
     */
    protected void onConnectFailure() {
        //Do something
    }


    /**
     * 如果socket建立好链接，会循环回调
     * @param stream socket 输入流，读取数据
     */
    protected void onSocketReady(InputStream stream) {
        //Do something
    }

    /**
     * 缓冲区有数据就会回调（数据是由pushToCache到缓冲区）
     * @param stream socket 输出流，发送数据
     */
    protected void onHasSendData(OutputStream stream) {
        //Do something

    }

    /**
     * 线程即将结束，在socket没关闭之前的回调
     */
    protected void onCloseSocket() {
        //Do something
    }


}
