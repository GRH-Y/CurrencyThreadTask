package com.jav.thread.message;


import com.jav.common.util.StringEnvoy;
import com.jav.thread.executor.LoopTaskExecutor;
import com.jav.thread.executor.TaskContainer;
import com.jav.thread.message.joggle.IMsgPostOffice;
import com.jav.thread.executor.ConsumerQueueAttribute;
import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.joggle.IAttribute;
import com.jav.thread.message.joggle.IMsgCourier;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 线程消息服务者
 * 负责线程间的通讯消息的分发
 *
 * @author yyz
 * @date 4/13/2017.
 */
public class MessagePostOffice implements IMsgPostOffice {

    /*** 消息通知数量最大时，超过则会引发开新线程处理*/
    private static final double TASK_EXEC_MAX_COUNT = 1000;
    private final AtomicBoolean mIsNotify;
    private final Map<String, MessageCourier> mCourierMap;
    private final Queue<ThreadHandler> mThreadList;
    private final Queue<MegOperation> mBacklogCache;


    public MessagePostOffice() {
        mIsNotify = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new HookThread());
        mCourierMap = new ConcurrentHashMap<>();
        mBacklogCache = new ConcurrentLinkedQueue<>();
        mThreadList = new ConcurrentLinkedQueue<>();
    }

    /**
     * 查找低压力的线程
     *
     * @return 返回低压力的线程
     */
    private ThreadHandler getNoBusyThread() {
        ThreadHandler handler = null;
        for (ThreadHandler tmp : mThreadList) {
            if (!tmp.isBusy()) {
                handler = tmp;
                break;
            }
        }
        if (handler == null) {
            handler = new ThreadHandler();
            mThreadList.add(handler);
        }
        return handler;
    }

    private void assignmentMsg(MessageEnvelope message) {
        ThreadHandler handler = getNoBusyThread();
        handler.getAttribute().pushToCache(message);
        if (!handler.getExecutor().isAliveState()) {
            handler.getExecutor().startTask();
        } else {
            handler.getExecutor().resumeTask();
        }
    }

    /**
     * 处理在分发数据中的没有注册的对象
     */
    private void clearBacklog() {
        while (!mBacklogCache.isEmpty()) {
            MegOperation entity = mBacklogCache.poll();
            if (entity == MegOperation.ADD) {
                registeredListener(entity.getCourier());
            } else if (entity == MegOperation.DEL) {
                unRegisteredListener(entity.getCourier());
            } else {
                clearAllMsgCourier();
            }
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public void sendEnvelope(MessageEnvelope message) {
        if (message != null) {
            if (message.isHighOverhead()) {
                assignmentMsg(message);
            } else {
                disposeMessage(message);
            }
        }
        clearBacklog();
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        for (ThreadHandler handler : mThreadList) {
            handler.getExecutor().stopTask();
        }
        mThreadList.clear();
        mBacklogCache.clear();
        clearAllMsgCourier();
    }


    /**
     * 向目标线程发送数据
     *
     * @param message 传递的数据
     */
    private void notifyTargetCourier(MessageEnvelope message) {
        String targetKey = message.getTargetKey();
        MessageCourier target = mCourierMap.get(targetKey);
        if (target != null) {
            message.setMsgPostOffice(this);
            target.onReceiveEnvelope(message);
        }
    }

    /**
     * 通知所有线程
     *
     * @param message 传递的数据
     */
    private void notifyAllCourier(MessageEnvelope message) {
        Set<Map.Entry<String, MessageCourier>> entrySet = mCourierMap.entrySet();
        for (Map.Entry<String, MessageCourier> entry : entrySet) {
            String key = entry.getKey();
            //不发给自己
            if (!key.equals(message.getSenderKey())) {
                MessageCourier target = entry.getValue();
                target.onReceiveEnvelope(message);
            }
        }
    }

    /**
     * 投递消息
     */
    private void disposeMessage(MessageEnvelope message) {
        mIsNotify.set(true);
        String targetKey = message.getTargetKey();
        if (StringEnvoy.isNotEmpty(targetKey) && !message.isRadio()) {
            notifyTargetCourier(message);
        } else {
            notifyAllCourier(message);
        }
        mIsNotify.set(false);
    }

    /**
     * 注册消息监听
     *
     * @param receive 消息接收者
     */
    protected void registeredListener(MessageCourier receive) {
        if (receive != null) {
            if (mIsNotify.get()) {
                mBacklogCache.offer(MegOperation.ADD.setCourier(receive));
            } else {
                String key = receive.getCourierKey();
                if (!mCourierMap.containsKey(key)) {
                    mCourierMap.put(key, receive);
                }
            }
        }
    }

    /**
     * 注销指定的监听器
     */
    protected void unRegisteredListener(MessageCourier receive) {
        if (receive != null && mCourierMap != null) {
            if (mIsNotify.get()) {
                mBacklogCache.offer(MegOperation.DEL.setCourier(receive));
            } else {
                mCourierMap.remove(receive.getCourierKey());
            }
        }
    }


    /**
     * 注销和清除所有的监听器
     */
    @Override
    public void clearAllMsgCourier() {
        if (mIsNotify.get()) {
            mBacklogCache.offer(MegOperation.DEL_ALL);
        } else {
            Set<Map.Entry<String, MessageCourier>> entrySet = mCourierMap.entrySet();
            for (Map.Entry<String, MessageCourier> entry : entrySet) {
                IMsgCourier receive = entry.getValue();
                receive.unRegMsgPostOffice(this);
            }
            mCourierMap.clear();
        }
    }

    /**
     * 处理消息线程
     *
     * @author prolog
     */
    private class ThreadHandler extends LoopTask {
        private boolean mIsBusy = false;

        private final LoopTaskExecutor mExecutor;
        private final IAttribute<MessageEnvelope> mAttribute;

        public ThreadHandler() {
            TaskContainer container = new TaskContainer(this, "MessagePostOffice.ThreadHandler");
            mExecutor = container.getTaskExecutor();
            mAttribute = new ConsumerQueueAttribute<>();
            mExecutor.startTask();
        }

        public LoopTaskExecutor getExecutor() {
            return mExecutor;
        }

        public IAttribute<MessageEnvelope> getAttribute() {
            return mAttribute;
        }

        @Override
        protected void onRunLoopTask() {
            do {
                MessageEnvelope envelope = mAttribute.popCacheData();
                if (envelope != null) {
                    disposeMessage(envelope);
//                    clearBacklog();
                    mIsBusy = mAttribute.getCacheDataSize() > TASK_EXEC_MAX_COUNT;
                }
            } while (mAttribute.getCacheDataSize() > 0);

            if (mAttribute.getCacheDataSize() == 0) {
                mExecutor.waitTask(8000);
                if (mAttribute.getCacheDataSize() == 0) {
                    mExecutor.stopTask();
                }
            }
        }

        private boolean isBusy() {
            return mIsBusy;
        }
    }


    /**
     * 虚拟机退出Hook线程
     *
     * @author prolog
     */
    private class HookThread extends Thread {
        @Override
        public void run() {
            release();
        }
    }

}
