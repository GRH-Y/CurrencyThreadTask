package task.executor.interfaces;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * socket网络通信任务控制接口
 * Created by dell on 3/27/2018.
 *
 * @author yyz
 */

public interface ISocketAttribute {

    void initSocket();

    void setSocket(Socket socket);


    /**
     * 设置网络连接超时
     *
     * @param timeout
     */
    void setTimeout(int timeout);

    /**
     * 切换端口，ip地址不变
     *
     * @param port 端口
     */
    void setPort(int port);

    /**
     * 切换ip地址,端口不变
     *
     * @param ip 地址
     */
    void setAddress(String ip);


    /**
     * 切换IP
     *
     * @param ip   ip地址
     * @param port 端口
     */
    void setAddressAndPort(String ip, int port);

    /**
     * 获取本地socket地址
     *
     * @return
     */
    String getLocalAddress();

    /**
     * 获取远程socket地址
     *
     * @return
     */
    String getRemoteAddress();

    /**
     * 获取本地端口
     *
     * @return
     */
    int getLocalPort();

    /**
     * 获取远程端口
     *
     * @return
     */
    int getRemotePort();

    InputStream getInputStream();

    OutputStream getOutputStream();


    /**
     * 当前网络连接状态
     *
     * @return
     */
    boolean isConnect();


    /**
     * 重新链接
     */
    void restartConnect();


    /**
     * 关闭socket
     */
    void closeSocket();


}
