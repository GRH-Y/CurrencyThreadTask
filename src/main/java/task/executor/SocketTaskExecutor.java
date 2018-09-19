package task.executor;


import task.executor.interfaces.IAttribute;
import task.executor.interfaces.ISocketAttribute;

/**
 * JavSocketConnect 使用socket通信
 * 默认处理了断线重新连接
 * Created by prolog on 7/5/2016.
 *
 * @author yyz
 */
public class SocketTaskExecutor<D> extends ConsumerTaskExecutor<D> {

    private ISocketAttribute mSocketAttribute;

    protected SocketTaskExecutor(TaskContainer container) {
        super(container);
        BaseSocketTask socketTask = container.getTask();
        mSocketAttribute = new SocketAttribute(this);
        executorTask = new SocketCoreTask(mSocketAttribute, socketTask);
    }

    @Override
    public <T> T getAttribute() {
        return (T) mSocketAttribute;
    }

    @Override
    public void setAttribute(IAttribute attribute) {
        mSocketAttribute = (ISocketAttribute) attribute;
    }
}
