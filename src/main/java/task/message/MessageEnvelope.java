package task.message;


import task.message.joggle.IEnvelope;
import task.message.joggle.IMsgPostOffice;

/**
 * 线程消息类
 * 线程之间的通讯
 *
 * @author yyz
 * @date 4/13/2017.
 */
public class MessageEnvelope implements IEnvelope {

    private String mMethodName = null;
    private boolean mHighOverhead = false;
    private boolean mRadio = true;
    private String mTargetKey = null;
    private String mSenderKey = null;
    private Object mData = null;
    private MegType mType = MegType.INSTANT;
    private IMsgPostOffice mSender = null;


    protected String getMethodName() {
        return mMethodName;
    }

    @Override
    public void setMethodName(String methodName) {
        mMethodName = methodName;
    }

    @Override
    public Object getData() {
        return mData;
    }

    @Override
    public void setData(Object data) {
        this.mData = data;
    }

    /**
     * 获取接收者key
     *
     * @return 返回接收者key
     */
    @Override
    public String getTargetKey() {
        return mTargetKey;
    }


    /**
     * 设置接收者key
     *
     * @param targetKey
     */
    @Override
    public void setTargetKey(String targetKey) {
        this.mTargetKey = targetKey;
        this.mRadio = false;
    }

    /**
     * 设置消息发送者的key
     *
     * @param senderKey 接收者key
     */
    protected void setSenderKey(String senderKey) {
        this.mSenderKey = senderKey;
    }

    /**
     * 获取发送本消息的接收者key
     *
     * @return 接收者key
     */
    @Override
    public String getSenderKey() {
        return mSenderKey;
    }

    /**
     * 消息类型
     *
     * @return 返回消息类型
     */
    @Override
    public MegType getType() {
        return mType;
    }


    public void setType(MegType type) {
        if (type != null) {
            this.mType = type;
        }
    }

    /**
     * 是否广播消息
     *
     * @return true 则是广播消息
     */
    @Override
    public boolean isRadio() {
        return mRadio;
    }

    /**
     * 设置是否广播消息
     *
     * @param radio true 则会广播给所有监听器
     */
    @Override
    public void setRadio(boolean radio) {
        this.mRadio = radio;
        if (mRadio) {
            this.mTargetKey = null;
        }
    }

    /**
     * 是否是高开销高效消息
     *
     * @return true为高开销
     */
    @Override
    public boolean isHighOverhead() {
        return mHighOverhead;
    }

    /**
     * 设置为true是以高效方式传送处理消息，默认为false
     *
     * @param highOverhead true为高开销
     */
    @Override
    public void setHighOverhead(boolean highOverhead) {
        this.mHighOverhead = highOverhead;
    }


    /**
     * 设置消息发送者
     *
     * @param sender 消息发送者
     */
    protected void setMsgPostOffice(IMsgPostOffice sender) {
        this.mSender = sender;
    }


    @Override
    public void sendToTarget(String methodName, Object data) {
        if (mSender == null) {
            throw new NullPointerException("The MsgPostOffice was not used and the specified MsgPostOffice was not found !");
        }
        setMethodName(methodName);
        setData(data);
        mSender.sendEnvelope(this);
    }
}
