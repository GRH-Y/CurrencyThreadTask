package task.message;


import task.executor.BaseConsumerTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.ConsumerTaskExecutor;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.message.joggle.IEnvelope;
import task.message.joggle.IMsgCourier;
import task.message.joggle.IMsgPostOffice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程消息服务者
 * 负责线程间的通讯消息的分发
 *
 * @author yyz
 * @date 4/13/2017.
 */
public class MessagePostOffice implements IMsgPostOffice {

    /*** 消息通知最大耗时，超过则会引发开新线程处理*/
    private static final double TASK_EXEC_MAX_TIME = 2 * 10e9;
    private volatile boolean isBusy = false;
    private volatile boolean isNotify = false;
    private Map<String, IMsgCourier> courierMap;
    private List<ThreadHandler> threadList;
    private List<IMsgPostOffice> msgPostOfficeList;
    private Queue<MegOperation> busyCache;


    public MessagePostOffice() {
        Runtime.getRuntime().addShutdownHook(new HookThread());
        courierMap = new ConcurrentHashMap<>();
        busyCache = new ConcurrentLinkedQueue();
        threadList = new ArrayList<>();
        msgPostOfficeList = new ArrayList<>();
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
                //Logcat.i("找到低压力的线程....");//测试
            }
        }
        if (handler == null) {
            //Logcat.i("没有空闲的线程，创建新的空闲线程....");//测试
            handler = new ThreadHandler();
            threadList.add(handler);
        }
        return handler;
    }

    private void assignmentMsg(IEnvelope message) {
        ThreadHandler handler = getNoBusyThread();
        if (!handler.getExecutor().getAliveState()) {
            handler.getAttribute().pushToCache(message);
            handler.getExecutor().startTask();
        } else {
            handler.getAttribute().pushToCache(message);
            handler.getExecutor().resumeTask();
        }
    }

    /**
     * 处理在分发数据中的没有注册的对象
     */
    private void execCache() {
        while (!busyCache.isEmpty()) {
            MegOperation entity = busyCache.remove();
            if (entity == MegOperation.ADD) {
                registeredListener(entity.getCourier());
            } else if (entity == MegOperation.DEL) {
                unRegisteredListener(entity.getCourier());
            } else {
                courierMap.clear();
            }
        }
    }

    @Override
    public int getMsgCourierCount() {
        return courierMap.size();
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public void sendEnvelope(IEnvelope message) {
        if (message != null) {
            if (message.isHighOverhead()) {
//                Logcat.i("即时消息开启线程执行....");//测试
                assignmentMsg(message);
            } else {
                if (isBusy) {
//                    Logcat.i("调用线程执行任务过于耗时，开启新线程执行....");//测试
                    assignmentMsg(message);
                } else {
//                    Logcat.i("调用线程执行....");//测试
                    isNotify = true;
                    disposeMessage(null, message);
                    execCache();
                }
            }
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        for (ThreadHandler handler : threadList) {
            handler.getExecutor().stopTask();
        }
        threadList.clear();
        removeAllNotifyListener();
//        Logcat.e("消息转发器正在销毁....");//测试
    }


    /**
     * 向目标线程发送数据
     *
     * @param message 传递的数据
     */
    private void notifyTargetCourier(IEnvelope message) {
        String targetKey = message.getTargetKey();
        if (courierMap.containsKey(targetKey)) {
            IMsgCourier target = courierMap.get(targetKey);
            message.setMsgPostOffice(this);
            target.onReceiveEnvelope(message);
        }
    }

    /**
     * 通知所有线程
     *
     * @param message 传递的数据
     */
    private void notifyAllCourier(IEnvelope message) {
        Set<Map.Entry<String, IMsgCourier>> entrySet = courierMap.entrySet();
        for (Map.Entry<String, IMsgCourier> entry : entrySet) {
            String key = entry.getKey();
            //不发给自己
            if (!key.equals(message.getSenderKey())) {
                IMsgCourier target = entry.getValue();
                target.onReceiveEnvelope(message);
            }
        }
    }

    private void notifyOtherPostOffice(IEnvelope message) {
        for (IMsgPostOffice office : msgPostOfficeList) {
            office.sendEnvelope(message);
        }
    }

    /**
     * 投递消息
     */
    private void disposeMessage(ThreadHandler handler, IEnvelope message) {
        //避免加线程锁
        isNotify = true;
        long sTime = System.nanoTime();
        sTime = sTime > 0 ? sTime : System.currentTimeMillis();
        if (message.isRadio()) {
            notifyAllCourier(message);
        } else {
            notifyTargetCourier(message);
        }
        long eTime = System.nanoTime();
        eTime = eTime > 0 ? eTime : System.currentTimeMillis();
        if (eTime - sTime > TASK_EXEC_MAX_TIME) {
            if (handler != null) {
                handler.setBusy(true);
                threadList.add(new ThreadHandler());
            } else {
                isBusy = true;
            }
        } else {
            if (handler != null) {
                handler.setBusy(false);
            }
        }
        notifyOtherPostOffice(message);
        isNotify = false;
    }

    @Override
    public void addIMsgPostOffice(IMsgPostOffice postOffice) {
        if (!msgPostOfficeList.contains(postOffice)) {
            msgPostOfficeList.add(postOffice);
        }
    }

    @Override
    public void removeIMsgPostOffice(IMsgPostOffice postOffice) {
        msgPostOfficeList.remove(postOffice);
    }

    @Override
    public void removeAllIMsgPostOffice() {
        msgPostOfficeList.clear();
    }

    /**
     * 注册消息监听
     *
     * @param receive 消息接收者
     */
    @Override
    public void registeredListener(IMsgCourier receive) {
        if (receive != null) {
            if (isNotify) {
                busyCache.add(MegOperation.ADD.setCourier(receive));
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
    @Override
    public void unRegisteredListener(IMsgCourier receive) {
        if (receive != null && courierMap != null) {
            if (isNotify) {
                busyCache.add(MegOperation.DEL.setCourier(receive));
            } else {
                courierMap.remove(receive.getCourierKey());
                receive.removeEnvelopeServer(this);
            }
        }
    }


    /**
     * 注销和清除所有的监听器
     */
    @Override
    public void removeAllNotifyListener() {
        if (!isNotify) {
            Set<Map.Entry<String, IMsgCourier>> entrySet = courierMap.entrySet();
            for (Map.Entry<String, IMsgCourier> entry : entrySet) {
                IMsgCourier receive = entry.getValue();
                if (isNotify) {
                    busyCache.add(MegOperation.DEL.setCourier(receive));
                }
                receive.removeEnvelopeServer(this);
            }
            if (isNotify) {
                busyCache.add(MegOperation.DEL_ALL);
            } else {
                courierMap.clear();
            }
        }
    }

    /**
     * 处理消息线程
     *
     * @author prolog
     */
    private class ThreadHandler extends BaseConsumerTask<IEnvelope> {
        private boolean isBusy = false;

        private TaskContainer container;
        private ConsumerTaskExecutor<IEnvelope> executor;
        private IConsumerAttribute<IEnvelope> attribute;

        public ThreadHandler() {
            container = new TaskContainer(this);
            executor = container.getTaskExecutor();
            attribute = new ConsumerQueueAttribute<>();
            container.setAttribute(attribute);
            executor.startTask();
        }

        public ConsumerTaskExecutor<IEnvelope> getExecutor() {
            return executor;
        }

        public IConsumerAttribute getAttribute() {
            return attribute;
        }

        @Override
        protected void onInitTask() {
            executor.setIdleStateSleep(true);
        }

        @Override
        protected void onProcess() {
            IEnvelope envelope = attribute.popCacheData();
            if (envelope != null) {
                isNotify = true;
                disposeMessage(this, envelope);
                execCache();
                if (attribute.getCacheDataSize() == 0) {
                    executor.waitTask(8000);
                    if (attribute.getCacheDataSize() == 0) {
                        executor.stopTask();
                    }
                }
            }
        }


        private boolean isBusy() {
            return isBusy;
        }

        private void setBusy(boolean busy) {
            isBusy = busy;
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
