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
    private IMsgPostOffice sender = null;


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
    public void setData(Object mData) {
        this.mData = mData;
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
     * @param mSenderKey 接收者key
     */
    protected void setSenderKey(String mSenderKey) {
        this.mSenderKey = mSenderKey;
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
     * @param mRadio true 则会广播给所有监听器
     */
    @Override
    public void setRadio(boolean mRadio) {
        this.mRadio = mRadio;
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
     * @param mHighOverhead true为高开销
     */
    @Override
    public void setHighOverhead(boolean mHighOverhead) {
        this.mHighOverhead = mHighOverhead;
    }


    /**
     * 设置消息发送者
     *
     * @param sender 消息发送者
     */
    protected void setMsgPostOffice(IMsgPostOffice sender) {
        this.sender = sender;
    }


    @Override
    public void sendToTarget(String methodName, Object data) {
        if (sender == null) {
            throw new NullPointerException("The MsgPostOffice was not used and the specified MsgPostOffice was not found !");
        }
        setMethodName(methodName);
        setData(data);
        sender.sendEnvelope(this);
    }
}
