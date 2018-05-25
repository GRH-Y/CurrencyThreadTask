package task.executor;


import task.executor.interfaces.IConsumerTaskExecutor;
import task.executor.interfaces.ITaskExecutor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 消费任务执行者
 * Created by dell on 2/9/2018.
 *
 * @author yyz
 */
public class ConsumerTaskExecutor<D> extends LoopTaskExecutor implements IConsumerTaskExecutor<D> {

    /**
     * 设置缓存区的大小
     * (特别注意:如果设置为小于1则存储为无限，当大于0时，如果当前缓存区数据大于该值，则会根据crowdOutModel来处理)
     */
    private int cacheMaxCount = 0;
    /***缓冲区*/
    private Queue<D> cache = new ConcurrentLinkedQueue();
    /***异步处理数据任务*/
    private ITaskExecutor asyncTaskExecutor = null;
    /***没有输任务则休眠标志位，为true则休眠*/
    private boolean isIdleStateSleep = false;
    /***true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存*/
    private boolean crowdOutModel = false;

    protected BaseConsumerTask consumerTask;
    private CoreTask coreTask;


    /**
     * 创建任务
     *
     * @param container 任务
     * @throws NullPointerException
     */
    protected ConsumerTaskExecutor(TaskContainer container) {
        super(container);
        this.consumerTask = container.getTask();
        coreTask = new CoreTask();
        loopTask = coreTask;
    }

    // -------------------start run ---------------------

    private class CoreTask extends BaseConsumerTask<D> {


        @Override
        protected void onInitTask() {
            consumerTask.onInitTask();
        }

        @Override
        protected void onRunLoopTask() {
            consumerTask.onRunLoopTask();
            onCreateData();
            // 没有数据是否需要线程休眠
            if (cache.size() == 0 && isIdleStateSleep) {
                waitTask();
//                waitCache(0);
            } else if (!isAsyncState() && getLoopState()) {
                onProcess();
            }
        }


        @Override
        protected void onCreateData() {
            consumerTask.onCreateData();
        }

        @Override
        protected void onProcess() {
            consumerTask.onProcess();
        }


        @Override
        protected void onIdleStop() {
            consumerTask.onIdleStop();
        }

        @Override
        protected void onDestroyTask() {
            consumerTask.onDestroyTask();
            clearCacheData();
        }
    }

    // -------------------End run ---------------------


    /**
     * 设置缓存区数量
     *
     * @param count 数量，默认是0无限大
     */
    @Override
    public void setCacheMaxCount(int count) {
        this.cacheMaxCount = count;
    }

    /**
     * 设置缓存区没有数据线程进入休眠
     *
     * @param state true 进入休眠状态
     */
    @Override
    public void setIdleStateSleep(boolean state) {
        this.isIdleStateSleep = state;
    }


    /**
     * 设置存储数据模式
     *
     * @param isCrowdOut true 则缓冲区满时挤掉最早的数据，false为缓冲区满则不保存
     */
    @Override
    public void setPushDataModel(boolean isCrowdOut) {
        this.crowdOutModel = isCrowdOut;
    }


    /**
     * 获取数据栈中的数据
     *
     * @return 返回栈低的数据
     */
    @Override
    public D popCacheData() {
        D data = null;
        if (!cache.isEmpty()) {
            data = cache.remove();
        }
        return data;
    }

    /**
     * 把数据压入数据栈
     *
     * @param data 数据
     */
    @Override
    public void pushToCache(D data) {
        if (!getLoopState() || data == null) {
            return;
        }
        if (cacheMaxCount > 0) {
            if (cache.size() >= cacheMaxCount && crowdOutModel) {
                cache.remove(0);
                cache.add(data);
                resumeTask();
//                restoreCache();
            }
        } else {
            cache.add(data);
            resumeTask();
//            restoreCache();
        }
    }

//    /**
//     * 恢复缓存数据进程
//     */
//    public void restoreCache() {
//        synchronized (cache) {
//            cache.notifyAll();
//        }
//    }
//
//    /**
//     * 暂停缓存数据进程
//     */
//    public void waitCache(long time) {
//        try {
//            synchronized (cache) {
//                cache.wait(time);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 获取同步任务当前缓冲区的大小
     *
     * @return 返回缓冲区大小
     */
    @Override
    public int getCacheDataSize() {
        return cache.size();
    }

    /**
     * 清理所有缓存要发送的数据
     */
    @Override
    public void clearCacheData() {
        if (!getIdleStopState()) {
            cache.clear();
        }
    }

    @Override
    public void stopTask() {
        super.stopTask();
        stopAsyncProcessData();
//        restoreCache();
    }

    /**
     * 异步处理数据线程
     */
    private class AsyncProcessDataTask extends BaseLoopTask {

        @Override
        public void onRunLoopTask() {
            if (cache.size() > 0) {
                coreTask.onProcess();
            } else {
                waitTask();
//                waitCache(0);
            }
        }

    }

    /**
     * 开启异步处理数据模式,
     * 开启后 onCreateData ,onProcess 分别不同线程来执行
     */
    public void startAsyncProcessData() {
        if (asyncTaskExecutor == null) {
            AsyncProcessDataTask asyncTask = new AsyncProcessDataTask();
            TaskContainer container = new TaskContainer(asyncTask);
            asyncTaskExecutor = container.getTaskExecutor();
            asyncTaskExecutor.startTask();
        }
    }

    /**
     * 关闭异步处理数据模式
     */
    public void stopAsyncProcessData() {
        if (asyncTaskExecutor != null) {
            asyncTaskExecutor.stopTask();
            asyncTaskExecutor = null;
        }
    }


    /**
     * 当前是否异步处理数据
     *
     * @return
     */
    public boolean isAsyncState() {
        return asyncTaskExecutor != null;
    }
}
