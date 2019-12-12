package task.message;


import task.executor.BaseLoopTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ITaskContainer;
import task.message.joggle.IMsgCourier;
import task.message.joggle.IMsgPostOffice;
import util.StringEnvoy;

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
    private volatile AtomicBoolean isNotify;
    private Map<String, MessageCourier> courierMap;
    private Queue<ThreadHandler> threadList;
    private Queue<MegOperation> backlogCache;


    public MessagePostOffice() {
        isNotify = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new HookThread());
        courierMap = new ConcurrentHashMap<>();
        backlogCache = new ConcurrentLinkedQueue();
        threadList = new ConcurrentLinkedQueue<>();
    }

    /**
     * 查找低压力的线程
     *
     * @return 返回低压力的线程
     */
    private ThreadHandler getNoBusyThread() {
        ThreadHandler handler = null;
        for (ThreadHandler tmp : threadList) {
            if (!tmp.isBusy()) {
                handler = tmp;
                break;
            }
        }
        if (handler == null) {
            handler = new ThreadHandler();
            threadList.add(handler);
        }
        return handler;
    }

    private void assignmentMsg(MessageEnvelope message) {
        ThreadHandler handler = getNoBusyThread();
        handler.getAttribute().pushToCache(message);
        if (!handler.getContainer().getTaskExecutor().getAliveState()) {
            handler.getContainer().getTaskExecutor().startTask();
        } else {
            handler.getContainer().getTaskExecutor().resumeTask();
        }
    }

    /**
     * 处理在分发数据中的没有注册的对象
     */
    private void clearBacklog() {
        while (!backlogCache.isEmpty()) {
            MegOperation entity = backlogCache.poll();
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
        for (ThreadHandler handler : threadList) {
            handler.getContainer().getTaskExecutor().stopTask();
        }
        threadList.clear();
        backlogCache.clear();
        clearAllMsgCourier();
    }


    /**
     * 向目标线程发送数据
     *
     * @param message 传递的数据
     */
    private void notifyTargetCourier(MessageEnvelope message) {
        String targetKey = message.getTargetKey();
        if (StringEnvoy.isNotEmpty(targetKey) && courierMap.containsKey(targetKey)) {
            MessageCourier target = courierMap.get(targetKey);
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
        Set<Map.Entry<String, MessageCourier>> entrySet = courierMap.entrySet();
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
        isNotify.set(true);
        if (message.isRadio()) {
            notifyAllCourier(message);
        } else {
            notifyTargetCourier(message);
        }
        isNotify.set(false);
    }

    /**
     * 注册消息监听
     *
     * @param receive 消息接收者
     */
    protected void registeredListener(MessageCourier receive) {
        if (receive != null) {
            if (isNotify.get()) {
                backlogCache.offer(MegOperation.ADD.setCourier(receive));
            } else {
                String key = receive.getCourierKey();
                if (!courierMap.containsKey(key)) {
                    courierMap.put(key, receive);
                }
            }
        }
    }

    /**
     * 注销指定的监听器
     */
    protected void unRegisteredListener(MessageCourier receive) {
        if (receive != null && courierMap != null) {
            if (isNotify.get()) {
                backlogCache.offer(MegOperation.DEL.setCourier(receive));
            } else {
                courierMap.remove(receive.getCourierKey());
            }
        }
    }


    /**
     * 注销和清除所有的监听器
     */
    @Override
    public void clearAllMsgCourier() {
        if (isNotify.get()) {
            backlogCache.offer(MegOperation.DEL_ALL);
        } else {
            Set<Map.Entry<String, MessageCourier>> entrySet = courierMap.entrySet();
            for (Map.Entry<String, MessageCourier> entry : entrySet) {
                IMsgCourier receive = entry.getValue();
                receive.unRegMsgPostOffice(this);
            }
            courierMap.clear();
        }
    }

    /**
     * 处理消息线程
     *
     * @author prolog
     */
    private class ThreadHandler extends BaseLoopTask {
        private boolean isBusy = false;

        private ITaskContainer container;
        private IConsumerAttribute<MessageEnvelope> attribute;

        public ThreadHandler() {
            container = new TaskContainer(this);
            attribute = new ConsumerQueueAttribute<>();
            container.getTaskExecutor().startTask();
        }

        public ITaskContainer getContainer() {
            return container;
        }

        public IConsumerAttribute<MessageEnvelope> getAttribute() {
            return attribute;
        }

        @Override
        protected void onRunLoopTask() {
            do {
                MessageEnvelope envelope = attribute.popCacheData();
                if (envelope != null) {
                    disposeMessage(envelope);
//                    clearBacklog();
                    isBusy = attribute.getCacheDataSize() > TASK_EXEC_MAX_COUNT;
                }
            } while (attribute.getCacheDataSize() > 0);

            if (attribute.getCacheDataSize() == 0) {
                container.getTaskExecutor().waitTask(8000);
                if (attribute.getCacheDataSize() == 0) {
                    container.getTaskExecutor().stopTask();
                }
            }
        }

        private boolean isBusy() {
            return isBusy;
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
