package com.jav.thread.message.joggle;


import com.jav.thread.message.MessageEnvelope;

/**
 * IMsgPostOffice 消息转发者
 * Created by prolog on 11/18/2016.
 *
 * @author yyz
 * @date 11/18/2016.
 */

public interface IMsgPostOffice {

    /**
     * 注销所有的监听器
     */
    void clearAllMsgCourier();

    /**
     * 发送消息
     *
     * @param message 消息
     */
    void sendEnvelope(MessageEnvelope message);

    /**
     * 释放资源
     */
    void release();
}
