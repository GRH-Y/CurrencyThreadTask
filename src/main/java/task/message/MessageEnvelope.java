package task.message;


import com.yyz.CurrencyThreadTask.task.message.interfaces.IEnvelope;
import com.yyz.CurrencyThreadTask.task.message.interfaces.IMsgPostOffice;

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
    private boolean mRadio = false;
    private String mTargetKey = null;
    private String mSenderKey = null;
    private Object mData = null;
    private MegType mType = null;
    private IMsgPostOffice sender = null;


    public MessageEnvelope() {
        init(MegType.INSTANT, null, false, true);
    }

    public MessageEnvelope(MegType type) {
        init(type, null, false, true);
    }

    public MessageEnvelope(String receiveKey) {
        init(mType, receiveKey, false, false);
    }

    /**
     * 线程消息
     *
     * @param type         消息类型
     * @param highOverhead 为true则以高效方式传送处理消息
     */
    public MessageEnvelope(MegType type, boolean highOverhead) {
        init(type, null, highOverhead, true);
    }

    public MessageEnvelope(String targetKey, boolean highOverhead) {
        init(mType, targetKey, highOverhead, false);
    }


    public MessageEnvelope(MegType type, String targetKey, boolean highOverhead) {
        init(type, targetKey, highOverhead, false);
    }


    private void init(MegType type, String targetKey, boolean highOverhead, boolean radio) {
        boolean existed = type == null || (targetKey == null && !radio);
        if (existed) {
            throw new NullPointerException("mType or mTargetKey cannot be null !");
        }
        this.mType = type;
        this.mTargetKey = targetKey;
        mHighOverhead = highOverhead;
        this.mRadio = radio;
    }

    @Override
    public String getMethodName() {
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
     * 设置消息发送者的key
     *
     * @param mSenderKey 接收者key
     */
    @Override
    public void setSenderKey(String mSenderKey) {
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
    @Override
    public void setMsgPostOffice(IMsgPostOffice sender) {
        this.sender = sender;
    }


    @Override
    public void sendToTarget(String methodName, Object data) {
        if (sender == null) {
            throw new NullPointerException("The MsgPostOffice was not used and the specified MsgPostOffice was not found !");
        }
        if (sender != null) {
            setMethodName(methodName);
            setData(data);
            sender.sendEnvelope(this);
        }
    }
}
