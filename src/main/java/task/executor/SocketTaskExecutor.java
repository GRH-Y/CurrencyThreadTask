package task.executor;


import com.yyz.CurrencyThreadTask.task.executor.interfaces.ISocketTaskExecutor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * JavSocketConnect 使用socket通信
 * 默认处理了断线重新连接
 * Created by prolog on 7/5/2016.
 *
 * @author yyz
 */
public class SocketTaskExecutor<D> extends ConsumerTaskExecutor<D> implements ISocketTaskExecutor<D> {

    private CoreTask coreTask;
    protected BaseSocketTask socketTask;

    private String ip;
    private int port;
    private Socket socket = null;
    /**
     * 输入流
     */
    private InputStream inputStream = null;
    /**
     * 输出流
     */
    private OutputStream outputStream = null;
    private boolean isConnect = false;
    private int timeout = 3000;

    protected SocketTaskExecutor(TaskContainer container) {
        super(container);
        this.socketTask = container.getTask();
        coreTask = new CoreTask();
        consumerTask = coreTask;
    }


    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * 切换端口，ip地址不变
     *
     * @param port 端口
     */
    @Override
    public void setPort(int port) {
        if (port > 0 && port <= 0xFFFF) {
            this.port = port;
        }
    }

    /**
     * 切换ip地址,端口不变
     *
     * @param ip 地址
     */
    @Override
    public void setAddress(String ip) {
        if (ip != null) {
            this.ip = ip;
        }
    }

    /**
     * 设置IP和端口
     *
     * @param ip   ip地址
     * @param port 端口
     */
    @Override
    public void setAddressAndPort(String ip, int port) {
        if (ip != null && port > 0 && port <= 0xFFFF) {
            this.ip = ip;
            this.port = port;
        }
    }

    @Override
    public String getLocalAddress() {
        return socket == null ? null : socket.getLocalAddress().getHostAddress();
    }

    @Override
    public String getRemoteAddress() {
        return socket == null ? null : ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return socket == null ? 0 : socket.getLocalPort();
    }

    @Override
    public int getRemotePort() {
        return socket == null ? 0 : socket.getPort();
    }


    /**
     * 获取当前socket状态
     *
     * @return
     */
    @Override
    public boolean isConnect() {
        return isConnect;
    }


    // -----------------------start 周期回调方法-------------------------------

    class CoreTask extends BaseSocketTask<D> {


        /**
         * 每次socket建立链接成功后回调一次
         */
        @Override
        protected void onConnectSuccess() {
            socketTask.onConnectSuccess();
        }

        /**
         * 每次socket建立链接失败后回调一次
         */
        @Override
        protected void onConnectFailure() {
            socketTask.onConnectFailure();
        }

        /**
         * 如果socket建立好链接，会循环回调
         *
         * @param stream socket 输入流
         */
        @Override
        protected void onSocketReady(InputStream stream) {
            socketTask.onSocketReady(stream);
        }

        /**
         * 缓冲区有数据就会回调（数据是由pushToCache到缓冲区）
         *
         * @param stream socket 输出流
         *               //         * @param data
         */
        @Override
        protected void onHasSendData(OutputStream stream) {
            socketTask.onHasSendData(stream);
        }

        /**
         * 线程即将结束，在socket没关闭之前的回调
         */
        @Override
        protected void onCloseSocket() {
            socketTask.onCloseSocket();
        }


        @Override
        protected void onInitTask() {
            socketTask.onInitTask();
            while (!isConnect && getLoopState()) {
                try {
                    if (socket == null) {
                        InetSocketAddress address = new InetSocketAddress(ip, port);
                        socket = new Socket();
                        socket.connect(address, timeout);
                    }
                    socket.setSoTimeout(timeout);
                    socket.setKeepAlive(true);
                    //复用端口
                    socket.setReuseAddress(true);
                    //关闭接收紧急数据
                    socket.setOOBInline(false);
                    //关闭Nagle算法
                    socket.setTcpNoDelay(true);
                    //执行Socket的close方法，该方法也会立即返回
                    socket.setSoLinger(true, 0);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    isConnect = true;
                    setLoopInit(false);
                    onConnectSuccess();
                } catch (Throwable e) {
                    closeSocket();
                    onConnectFailure();
                    if (e instanceof ConnectException && ip != null) {
                        sleepTask(1000);
                    }
                    inputStream = null;
                    outputStream = null;
                }
            }
        }

        @Override
        protected void onIdleStop() {
            socketTask.onIdleStop();
        }

        @Override
        protected void onRunLoopTask() {
            socketTask.onRunLoopTask();
        }

        @Override
        protected void onDestroyTask() {
            socketTask.onDestroyTask();
            onCloseSocket();
            closeSocket();
        }

        @Override
        protected void onProcess() {
            socketTask.onProcess();
            onHasSendData(outputStream);
        }


        @Override
        protected void onCreateData() {
            socketTask.onCreateData();
            onSocketReady(inputStream);
        }

    }

    // -----------------------end 周期回调方法 -------------------------------


    @Override
    public void pushToCache(D data) {
        if (isConnect) {
            super.pushToCache(data);
        }
    }


    /**
     * 关闭socket
     */
    protected void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket = null;
                outputStream = null;
                inputStream = null;
                isConnect = false;
            }
        }
    }


    /**
     * 重新链接
     */
    @Override
    public void restartConnect() {
        closeSocket();
        if (ip != null) {
            setLoopInit(true);
        } else {
            //如果ip为空，则该任务没必要继续运行
            stopTask();
        }
    }


}
