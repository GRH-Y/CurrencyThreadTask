package task.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射缓存（提升反射性能）
 * Created by dell on 3/14/2018.
 *
 * @author yyz
 */
public class ReflectionCache {
    private static ReflectionCache cache = null;
    private static Map<String, Class> classMap = null;
    private static Map<String, Method> methodMap = null;
    private static Map<String, Method[]> methodsMap = null;
    private static Map<String, Field> fieldMap = null;
    private static Map<String, Field[]> fieldsMap = null;

    private ReflectionCache() {
        classMap = new HashMap<>();
        methodMap = new HashMap<>();
        methodsMap = new HashMap<>();
        fieldMap = new HashMap<>();
        fieldsMap = new HashMap<>();
    }

    public synchronized static ReflectionCache getCache() {
        if (cache == null) {
            synchronized (ReflectionCache.class) {
                if (cache == null) {
                    cache = new ReflectionCache();
                }
            }
        }
        return cache;
    }

    public void setClass(Class clx) {
        Class tmp = classMap.get(clx.getName());
        if (tmp == null) {
            classMap.put(clx.getName(), clx);
        }
    }

    public Class getClass(Object object) {
        return getClass(object.getClass().getName());
    }

    public Class getClass(String className) {
        Class clx = classMap.get(className);
        if (clx == null) {
            try {
                clx = Class.forName(className);
                classMap.put(className, clx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return clx;
    }

    public Method getMethod(Object object, String methodName, Class<?>... parameterTypes) {
        return getMethod(object.getClass().getName(), methodName, parameterTypes);
    }

    public Method getMethod(Class clx, String methodName, Class<?>... parameterTypes) {
        return getMethod(clx.getName(), methodName, parameterTypes);
    }

    public Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Class clx = getClass(className);
        String key = getMethodKey(className, methodName, parameterTypes);
        Method method = methodMap.get(key);
        if (method == null) {
            try {
                method = clx.getDeclaredMethod(methodName, parameterTypes);
                methodMap.put(key, method);
            } catch (Exception e) {
                Class supperClx = clx.getSuperclass();
                if (supperClx != Object.class) {
                    return getMethod(supperClx, methodName, parameterTypes);
                }
            }
        }
        return method;
    }

    public Method[] getAllMethod(Class clx) {
        setClass(clx);
        String key = getMethodKey(clx.getName(), "");
        Method[] method = methodsMap.get(key);
        if (method == null) {
            try {
                method = clx.getMethods();
                methodsMap.put(key, method);
            } catch (Exception e) {
                try {
                    method = clx.getDeclaredMethods();
                    methodsMap.put(key, method);
                } catch (Exception e1) {
                }
            }
        }
        return method;
    }

    public Field getField(Class clx, String fieldName) {
        Field field;
        String key = getFieldKey(clx.getName(), fieldName);
        field = fieldMap.get(key);
        if (field == null) {
            try {
                field = clx.getDeclaredField(fieldName);
                fieldMap.put(key, field);
            } catch (Exception e) {
                Class supperClx = clx.getSuperclass();
                if (supperClx != Object.class) {
                    return getField(supperClx, fieldName);
                } else {
                    return null;
                }
            }
        }
        return field;
    }

    public Field[] getAllField(Class clx) {
        Field[] field;
        String key = getFieldKey(clx.getName(), "");
        field = fieldsMap.get(key);
        if (field == null) {
            try {
                field = clx.getDeclaredFields();
                fieldsMap.put(key, field);
            } catch (Exception e) {
                Class supperClx = clx.getSuperclass();
                if (supperClx != Object.class) {
                    return getAllField(supperClx);
                } else {
                    return null;
                }
            }
        }
        return field;
    }

    private String getFieldKey(String className, String fieldName) {
        StringBuilder builder = new StringBuilder();
        builder.append(className);
        builder.append("-");
        builder.append(fieldName);
        return builder.toString();
    }

    private String getMethodKey(String className, String methodName, Class... parameterTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append(className);
        builder.append("-");
        builder.append(methodName);
        builder.append("-");
        for (Class clx : parameterTypes) {
            builder.append(clx.getName());
        }
        return builder.toString();
    }

    public synchronized static void release() {
        if (classMap != null) {
            classMap.clear();
            classMap = null;
        }
        if (methodMap != null) {
            methodMap.clear();
            methodMap = null;
        }
        if (fieldMap != null) {
            fieldMap.clear();
            fieldMap = null;
        }
        cache = null;
    }


}
