package task.executor;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * JavSocketConnect 使用socket通信
 * 默认处理了断线重新连接
 * Created by prolog on 7/5/2016.
 *
 * @author yyz
 */
public class SocketTaskExecutor<D> extends ConsumerTaskExecutor<D> {

    protected BaseSocketTask socketTask;

    protected SocketTaskExecutor(TaskContainer container) {
        super(container);
        this.socketTask = container.getTask();
        consumerTask = new CoreTask();
    }


    // -----------------------start 周期回调方法-------------------------------

    class CoreTask extends BaseSocketTask<D> {

        private SocketAttribute socketAttribute;

        public CoreTask() {
            socketAttribute = new SocketAttribute(SocketTaskExecutor.this);
        }

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
            socketAttribute.initSocket();
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
            socketAttribute.closeSocket();
        }

        @Override
        protected void onProcess() {
            socketTask.onProcess();
            onHasSendData(socketAttribute.getOutputStream());
        }


        @Override
        protected void onCreateData() {
            socketTask.onCreateData();
            onSocketReady(socketAttribute.getInputStream());
        }

    }

    // -----------------------end 周期回调方法 -------------------------------

}
