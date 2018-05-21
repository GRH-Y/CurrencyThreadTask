package task.executor;


/**
 * 消费任务
 * * onCreateData 生成消费数据
 * onProcessData 处理消费数据
 * 例子，网络请求onCreateData接收数据，在onProcessData处理接收数据
 * Created by No.9 on 2018/2/18.
 *
 * @author yyz
 */
public class BaseConsumerTask<D> extends BaseLoopTask {

    /**
     * 生产消费数据
     */
    protected void onCreateData() {
    }

//    /**
//     * 处理消费数据
//     *
//     * @param data
//     */
//    protected void onProcessData(D data) {
//    }


    protected void onProcess() {

    }
}
