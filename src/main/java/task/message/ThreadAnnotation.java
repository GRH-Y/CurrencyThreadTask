package task.message;


import util.SpeedReflex;
import util.StringUtils;

import java.lang.reflect.Method;

/**
 * 线程消息分发注解调用
 *
 * @author yyz
 * @date 7/4/2017.
 * Created by No.9 on 7/4/2017.
 */
public class ThreadAnnotation {

    private ThreadAnnotation() {
        throw new IllegalStateException("cannot new ThreadAnnotation class");
    }

    /**
     * invoke methodName for target
     *
     * @param methodName 方法名
     * @param target     target class
     * @param data       data
     */
    public static void disposeMessage(String methodName, Object target, Object data) {
        if (StringUtils.isEmpty(methodName) || target == null) {
            return;
        }
        try {
            SpeedReflex reflex = SpeedReflex.getCache();
            Class cls = target.getClass();
            if (data == null) {
                Method method = reflex.getMethod(cls, methodName);
                method.setAccessible(true);
                method.invoke(target);
            } else {
                Method method = reflex.getMethod(cls, methodName, data.getClass());
                method.setAccessible(true);
                method.invoke(target, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
