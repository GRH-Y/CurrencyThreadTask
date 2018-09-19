package task.executor;

import task.executor.interfaces.ISocketAttribute;

import java.io.InputStream;
import java.io.OutputStream;

public class SocketCoreTask<D> extends BaseSocketTask<D> {

    private ISocketAttribute mSocketAttribute;
    protected BaseSocketTask mSocketTask;

    public SocketCoreTask(ISocketAttribute attribute, BaseSocketTask socketTask) {
        this.mSocketTask = socketTask;
        this.mSocketAttribute = attribute;
    }

    /**
     * 每次socket建立链接成功后回调一次
     */
    @Override
    protected void onConnectSuccess() {
        mSocketTask.onConnectSuccess();
    }

    /**
     * 每次socket建立链接失败后回调一次
     */
    @Override
    protected void onConnectFailure() {
        mSocketTask.onConnectFailure();
    }

    /**
     * 如果socket建立好链接，会循环回调
     *
     * @param stream socket 输入流
     */
    @Override
    protected void onSocketReady(InputStream stream) {
        mSocketTask.onSocketReady(stream);
    }

    /**
     * 缓冲区有数据就会回调（数据是由pushToCache到缓冲区）
     *
     * @param stream socket 输出流
     *               //         * @param data
     */
    @Override
    protected void onHasSendData(OutputStream stream) {
        mSocketTask.onHasSendData(stream);
    }

    /**
     * 线程即将结束，在socket没关闭之前的回调
     */
    @Override
    protected void onCloseSocket() {
        mSocketTask.onCloseSocket();
    }


    @Override
    protected void onInitTask() {
        mSocketTask.onInitTask();
        mSocketAttribute.initSocket();
    }

    @Override
    protected void onIdleStop() {
        mSocketTask.onIdleStop();
    }

    @Override
    protected void onRunLoopTask() {
        mSocketTask.onRunLoopTask();
    }

    @Override
    protected void onDestroyTask() {
        mSocketTask.onDestroyTask();
        onCloseSocket();
        mSocketAttribute.closeSocket();
    }

    @Override
    protected void onProcess() {
        mSocketTask.onProcess();
        onHasSendData(mSocketAttribute.getOutputStream());
    }


    @Override
    protected void onCreateData() {
        mSocketTask.onCreateData();
        onSocketReady(mSocketAttribute.getInputStream());
    }
}
