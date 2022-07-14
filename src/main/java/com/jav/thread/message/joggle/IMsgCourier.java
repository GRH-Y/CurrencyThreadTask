package com.jav.thread.message.joggle;

import com.jav.thread.message.MessagePostOffice;
import com.jav.thread.message.MessageEnvelope;

/**
 * IMsgCourier 消息接收者
 * Created by prolog on 6/20/2016.
 *
 * @author yyz
 * @date 6/20/2016.
 */
public interface IMsgCourier {
    /**
     * 发送消息（使用代理传递消息）
     *
     * @param message 消息
     */
    void sendEnvelopeProxy(MessageEnvelope message);


    /**
     * 发送消息(传递消息不用代理)
     *
     * @param message 消息
     */
    void sendEnvelopSelf(MessageEnvelope message);


    /**
     * 设置消息传递者
     *
     * @param postOffice 消息传递者
     */
    void regMsgPostOffice(MessagePostOffice postOffice);

    /**
     * 移除消息传递者
     *
     * @param postOffice 消息传递者
     */
    void unRegMsgPostOffice(MessagePostOffice postOffice);

    /**
     * 获取消息接收者唯一标识符
     *
     * @return 返回唯一标识符
     */
    String getCourierKey();

    /**
     * 释放资源
     */
    void release();

}
