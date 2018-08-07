package task.executor;


import task.executor.interfaces.ISocketAttribute;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketAttribute implements ISocketAttribute {

    private SocketTaskExecutor mTaskExecutor;

    private Socket mSocket = null;
    private String ip;
    private int port;
    private int timeout = 3000;
    private boolean isConnect = false;

    /**
     * 输入流
     */
    private InputStream inputStream = null;
    /**
     * 输出流
     */
    private OutputStream outputStream = null;

    public SocketAttribute(SocketTaskExecutor executor) {
        this.mTaskExecutor = executor;
    }

    @Override
    public void initSocket() {
        while (!isConnect && mTaskExecutor.getLoopState()) {
            try {
                if (mSocket == null) {
                    InetSocketAddress address = new InetSocketAddress(ip, port);
                    mSocket = new Socket();
                    mSocket.connect(address, timeout);
                }
                mSocket.setSoTimeout(timeout);
                mSocket.setKeepAlive(true);
                //复用端口
                mSocket.setReuseAddress(true);
                //关闭接收紧急数据
                mSocket.setOOBInline(false);
                //关闭Nagle算法
                mSocket.setTcpNoDelay(true);
                //执行Socket的close方法，该方法也会立即返回
                mSocket.setSoLinger(true, 0);
                inputStream = mSocket.getInputStream();
                outputStream = mSocket.getOutputStream();
                isConnect = true;
                mTaskExecutor.setLoopInit(false);
                BaseSocketTask socketTask = (BaseSocketTask) mTaskExecutor.loopTask;
                socketTask.onConnectSuccess();
            } catch (Throwable e) {
                BaseSocketTask socketTask = (BaseSocketTask) mTaskExecutor.loopTask;
                closeSocket();
                socketTask.onConnectFailure();
                if (e instanceof ConnectException && ip != null) {
                    mTaskExecutor.sleepTask(1000);
                }
                inputStream = null;
                outputStream = null;
            }
        }
    }


    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setSocket(Socket socket) {
        this.mSocket = socket;
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
        return mSocket == null ? null : mSocket.getLocalAddress().getHostAddress();
    }

    @Override
    public String getRemoteAddress() {
        return mSocket == null ? null : ((InetSocketAddress) mSocket.getRemoteSocketAddress()).getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return mSocket == null ? 0 : mSocket.getLocalPort();
    }

    @Override
    public int getRemotePort() {
        return mSocket == null ? 0 : mSocket.getPort();
    }


    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
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


    /**
     * 重新链接
     */
    @Override
    public void restartConnect() {
        closeSocket();
        if (ip != null) {
            mTaskExecutor.setLoopInit(true);
        } else {
            //如果ip为空，则该任务没必要继续运行
            mTaskExecutor.stopTask();
        }
    }

    /**
     * 关闭socket
     */
    @Override
    public void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mSocket = null;
                outputStream = null;
                inputStream = null;
                isConnect = false;
            }
        }
    }
}
