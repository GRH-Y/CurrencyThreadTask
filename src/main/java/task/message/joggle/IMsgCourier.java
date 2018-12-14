package task.message.joggle;

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
    void sendEnvelopeProxy(IEnvelope message);


    /**
     * 发送消息(传递消息不用代理)
     *
     * @param message 消息
     */
    void sendEnvelopSelf(IEnvelope message);


    /**
     * 消息接收回调接口
     *
     * @param message 消息
     */
    void onReceiveEnvelope(IEnvelope message);

    /**
     * 设置消息传递者
     *
     * @param postOffice 消息传递者
     */
    void addEnvelopeServer(IMsgPostOffice postOffice);

    /**
     * 移除消息传递者
     *
     * @param postOffice 消息传递者
     */
    void removeEnvelopeServer(IMsgPostOffice postOffice);

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
