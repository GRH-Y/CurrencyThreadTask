package task.message.interfaces;


/**
 * IMsgPostOffice 消息转发者
 * Created by prolog on 11/18/2016.
 *
 * @author yyz
 * @date 11/18/2016.
 */

public interface IMsgPostOffice {
    /**
     * 添加消息传递着
     *
     * @param postOffice 消息传递着
     */
    void addIMsgPostOffice(IMsgPostOffice postOffice);

    /**
     * 移除指定的消息传递着
     *
     * @param postOffice 消息传递着
     */
    void removeIMsgPostOffice(IMsgPostOffice postOffice);

    /**
     * 移除所有的IMsgPostOffice
     */
    void removeAllIMsgPostOffice();

    /**
     * 注册消息监听
     *
     * @param receive 消息接收者
     */
    void registeredListener(IMsgCourier receive);

    /**
     * 注销指定的监听器
     *
     * @param receive 消息接收者
     */
    void unRegisteredListener(IMsgCourier receive);

    /**
     * 注销所有的监听器
     */
    void removeAllNotifyListener();

    /**
     * 获取消息接收者数量
     *
     * @return 返回 消息接收者数量
     */
    int getMsgCourierCount();

    /**
     * 发送消息
     *
     * @param message 消息
     */
    void sendEnvelope(IEnvelope message);

    /**
     * 释放资源
     */
    void release();
}
