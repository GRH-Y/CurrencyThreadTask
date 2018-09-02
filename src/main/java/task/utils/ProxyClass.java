package com.yyz.android.uiaf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * 代理
 * Created by yyz on 1/11/2018.
 */

public class ProxyClass implements InvocationHandler {

    private InvocationHandler mInvocationHandler = null;

    private ProxyClass(InvocationHandler handler) {
        mInvocationHandler = handler;
    }


    /**
     * 创建代理类
     *
     * @param apiClass api接口
     * @param handler  接口回调处理类
     * @param <T>      api接口使用的数据类型
     * @return 返回api接口实例
     */
    public static <T> T newInstance(Class<T> apiClass, InvocationHandler handler) {
        Class<?>[] interfaces = new Class<?>[]{apiClass};
        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(), interfaces, new ProxyClass(handler));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        System.out.println("调用的方法名称为:" + methodName);

        return proxy;
    }

}
