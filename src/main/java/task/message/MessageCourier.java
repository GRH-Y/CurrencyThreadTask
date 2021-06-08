package task.message;


import task.message.joggle.IEnvelope;
import task.message.joggle.IMsgCourier;
import task.message.joggle.IMsgPostOffice;
import task.message.joggle.INotifyListener;
import util.ReflectionCall;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MessageCourier 线程消息收发者
 *
 * @author yyz
 * @date 4/10/2017.
 * Created by prolog on 4/10/2017.
 */
public class MessageCourier implements IMsgCourier {
    protected INotifyListener listener;
    protected Object target = null;
    private final String courierKey;

    /***消息栈*/
    protected Queue<IEnvelope> msgQueue = new ConcurrentLinkedQueue();
    private final Queue<MessagePostOffice> serverQueue = new ConcurrentLinkedQueue();

    public MessageCourier(INotifyListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        courierKey = toString();
        this.listener = listener;
    }

    public MessageCourier(Object target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }
        courierKey = toString();
        this.target = target;
    }


    /**
     * //线程间通信接口，其它线程发送数据可以在这里收到
     *
     * @param message 传递的数据
     */
    protected void onReceiveEnvelope(MessageEnvelope message) {
        switch (message.getType()) {
            //即时消息
            case INSTANT:
            default:
                if (listener == null) {
                    ReflectionCall.invoke(target, message.getMethodName(), new Class[]{MessageEnvelope.class}, message);
                } else {
                    listener.onInstantMessage(message);
                }
                break;
            //非即时消息
            case MAIL:
                msgQueue.add(message);
                break;
        }
    }

    @Override
    public String getCourierKey() {
        return courierKey;
    }

    /**
     * 取出消息
     *
     * @return 返回消息
     */
    public IEnvelope popMessage() {
        IEnvelope data = null;
        if (!msgQueue.isEmpty()) {
            data = msgQueue.remove();
        }
        return data;
    }


    /**
     * 设置消息发送者
     *
     * @param postOffice 消息发送者
     */
    @Override
    public void regMsgPostOffice(MessagePostOffice postOffice) {
        if (!serverQueue.contains(postOffice)) {
            serverQueue.offer(postOffice);
            postOffice.registeredListener(this);
        }
    }

    @Override
    public void unRegMsgPostOffice(MessagePostOffice postOffice) {
        if (serverQueue.contains(postOffice)) {
            serverQueue.remove(postOffice);
            postOffice.unRegisteredListener(this);
        }
    }


    /**
     * 发送消息（使用代理传递消息）
     *
     * @param message 消息
     */
    @Override
    public void sendEnvelopeProxy(MessageEnvelope message) {
        if (message != null) {
            message.setHighOverhead(true);
            sendEnvelop(message);
        }
    }

    private void sendEnvelop(MessageEnvelope message) {
        message.setSenderKey(courierKey);
        Iterator<MessagePostOffice> iterator = serverQueue.iterator();
        while (iterator.hasNext()) {
            IMsgPostOffice sender = iterator.next();
            sender.sendEnvelope(message);
        }
    }

    /**
     * 发送消息(传递消息不用代理)
     *
     * @param message 消息
     */
    @Override
    public void sendEnvelopSelf(MessageEnvelope message) {
        if (message != null) {
            message.setHighOverhead(false);
            sendEnvelop(message);
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        Iterator<MessagePostOffice> iterator = serverQueue.iterator();
        while (iterator.hasNext()) {
            MessagePostOffice sender = iterator.next();
            sender.unRegisteredListener(this);
        }
        serverQueue.clear();
        msgQueue.clear();
    }

}
