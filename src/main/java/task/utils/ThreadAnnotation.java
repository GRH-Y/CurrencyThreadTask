package task.utils;


import java.lang.reflect.Method;

/**
 * 线程消息分发注解调用
 *
 * @author yyz
 * @date 7/4/2017.
 * Created by No.9 on 7/4/2017.
 */
public class ThreadAnnotation {

    /**
     * invoke methodName for target
     *
     * @param methodName 方法名
     * @param target     target class
     * @param data       data
     */
    public static void disposeMessage(String methodName, Object target, Object data) {
        if (methodName == null || target == null) {
            return;
        }
        try {
            ReflectionCache cache = ReflectionCache.getCache();
            Class cls = target.getClass();
            if (data == null) {
                Method method = cache.getMethod(cls, methodName);
                method.setAccessible(true);
                method.invoke(target);
            } else {
                Method method = cache.getMethod(cls, methodName, data.getClass());
                method.setAccessible(true);
                method.invoke(target, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
