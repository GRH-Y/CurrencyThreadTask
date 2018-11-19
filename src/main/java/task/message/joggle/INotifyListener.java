package task.message.joggle;

/**
 * 线程消息回调接口
 * Created by prolog on 4/12/2017.
 *
 * @author yyz
 * @date 4/12/2017.
 */
public interface INotifyListener {
    /**
     * 即时消息接口 （执行者是非本线程）
     *
     * @param message 消息
     */
    void onInstantMessage(IEnvelope message);

    /**
     * 非即时消息接口（执行者是本线程）
     *
     * @param message 消息
     */
    void onMailMessage(IEnvelope message);
}
