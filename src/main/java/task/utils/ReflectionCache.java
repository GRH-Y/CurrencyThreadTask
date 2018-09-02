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
    private static volatile ReflectionCache cache = null;
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

    public static ReflectionCache getCache() {
        ReflectionCache tmp = cache;
        if (cache == null) {
            synchronized (ReflectionCache.class) {
                tmp = cache;
                if (cache == null) {
                    cache = new ReflectionCache();
                    tmp = cache;
                }
            }
        }
        return tmp;
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
        return classMap.computeIfAbsent(className, k -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        });
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
            method = methodMap.computeIfAbsent(key, k -> {
                try {
                    return clx.getDeclaredMethod(methodName, parameterTypes);
                } catch (Exception e) {
                    Class supperClx = clx.getSuperclass();
                    if (supperClx != Object.class) {
                        return getMethod(supperClx, methodName, parameterTypes);
                    }
                }
                return null;
            });
        }
        return method;
    }

    public Method[] getAllMethod(Class clx) {
        setClass(clx);
        String key = getMethodKey(clx.getName(), "");
        Method[] method = methodsMap.get(key);
        if (method == null) {
            method = methodsMap.computeIfAbsent(key, k -> {
                try {
                    return clx.getMethods();
                } catch (Exception e) {
                    try {
                        return clx.getDeclaredMethods();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                return null;
            });
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

        String key = getFieldKey(clx.getName(), "");
        return fieldsMap.computeIfAbsent(key, k -> {
            try {
                return clx.getDeclaredFields();
            } catch (Exception e) {
                Class supperClx = clx.getSuperclass();
                if (supperClx != Object.class) {
                    return getAllField(supperClx);
                }
            }
            return null;
        });
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

    public static synchronized void release() {
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
