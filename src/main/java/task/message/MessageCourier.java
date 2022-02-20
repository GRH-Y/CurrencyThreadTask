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
    protected INotifyListener mListener;
    protected Object mTarget = null;
    private final String mCourierKey;

    /***消息栈*/
    protected final Queue<IEnvelope> mMsgQueue = new ConcurrentLinkedQueue<>();
    private final Queue<MessagePostOffice> mServerQueue = new ConcurrentLinkedQueue<>();

    public MessageCourier(INotifyListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        mCourierKey = toString();
        this.mListener = listener;
    }

    public MessageCourier(Object target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }
        mCourierKey = toString();
        this.mTarget = target;
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
                if (mListener == null) {
                    ReflectionCall.invoke(mTarget, message.getMethodName(), new Class[]{MessageEnvelope.class}, message);
                } else {
                    mListener.onInstantMessage(message);
                }
                break;
            //非即时消息
            case MAIL:
                mMsgQueue.add(message);
                break;
        }
    }

    @Override
    public String getCourierKey() {
        return mCourierKey;
    }

    /**
     * 取出消息
     *
     * @return 返回消息
     */
    public IEnvelope popMessage() {
        IEnvelope data = null;
        if (!mMsgQueue.isEmpty()) {
            data = mMsgQueue.remove();
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
        if (!mServerQueue.contains(postOffice)) {
            mServerQueue.offer(postOffice);
            postOffice.registeredListener(this);
        }
    }

    @Override
    public void unRegMsgPostOffice(MessagePostOffice postOffice) {
        if (mServerQueue.contains(postOffice)) {
            mServerQueue.remove(postOffice);
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
        message.setSenderKey(mCourierKey);
        Iterator<MessagePostOffice> iterator = mServerQueue.iterator();
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
        Iterator<MessagePostOffice> iterator = mServerQueue.iterator();
        while (iterator.hasNext()) {
            MessagePostOffice sender = iterator.next();
            sender.unRegisteredListener(this);
        }
        mServerQueue.clear();
        mMsgQueue.clear();
    }

}
