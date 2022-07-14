package com.jav.thread.message.joggle;

/**
 * IEnvelope
 *
 * @author yyz
 * @date 4/13/2017.
 * Created by prolog on 4/13/2017.
 */
public interface IEnvelope {
    /**
     * 消息类型
     * INSTANT 即时消息 （发送消息者处理消息）
     * MAIL 非即时消息（接收目标者处理消息）
     */
    enum MegType {
        INSTANT, MAIL
    }


    void setMethodName(String methodName);

    /**
     * 获取接收消息者
     *
     * @return 返回接收者
     */
    Object getData();

    /**
     * 设置消息接收者
     *
     * @param mData 消息接收者
     */
    void setData(Object mData);


    /**
     * 获取发送者的密钥
     *
     * @return 密钥
     */
    String getSenderKey();

    /**
     * 获取接收者的密钥
     *
     * @return 密钥
     */
    String getTargetKey();

    /**
     * 设置接收者key
     * @param targetKey
     */
    void setTargetKey(String targetKey);

    /**
     * 获取该消息的类型
     *
     * @return
     * @see MegType
     */
    MegType getType();


    void setType(MegType type);

    /**
     * 该消息是否广播消息（此类型消息所有接收类都能接收）
     *
     * @return true 为广播消息
     */
    boolean isRadio();

    /**
     * 设置消息是否为广播消息类型
     *
     * @param mRadio true 为广播消息
     */
    void setRadio(boolean mRadio);

    /**
     * 该消息是否即时消息
     *
     * @return true为即时消息
     */
    boolean isHighOverhead();

    /**
     * 设置该消息为即时消息
     *
     * @param mHighOverhead true为即时消息
     */
    void setHighOverhead(boolean mHighOverhead);

    /**
     * 回应消息给发送者
     *
     * @param methodName 接收该消息的方法名
     * @param data       消息内容
     */
    void sendToTarget(String methodName, Object data);

}
