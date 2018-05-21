package task.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logcat 日志工厂,通用java与android平台
 * Ceated by prolog on 10/17/2016.
 *
 * @author yyz
 * @date 10/17/2016.
 */

public class Logcat {
    private final static String NULL = "";
    private static boolean IS_DEBUG = true;
    private static Class<?> cls;

    static {
        try {
            cls = Class.forName("android.util.Log");
        } catch (Exception e) {
            cls = null;
        }
    }

    public static void setIsDebug(boolean isDebug) {
        Logcat.IS_DEBUG = isDebug;
    }

    private Logcat() {
    }


    /**
     * 保存日志
     *
     * @param path 保存路径
     * @param log  日志内容
     */
    public synchronized static void saveLog(String path, String log) {
        if (path == null || log == null) {
            return;
        }
        File dir;
        File file;
        try {
            dir = new File(path);
            if (dir.exists() == false) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            return;
        }
        long time = System.currentTimeMillis() / (864 * 100000);
        DirFilter filter = new DirFilter(String.valueOf(time));
        String[] strings = dir.list(filter);
        if (strings.length > 0) {
            if (strings.length == 1) {
                file = new File(dir, strings[0]);
                if (file.exists() && file.length() > 1024 * 2) {
                    file = new File(dir, time + "-" + 1 + ".log");
                }
            } else {
                int index = 0;
                for (String name : strings) {
                    String[] str = name.split("-");
                    if (str.length == 2) {
                        str = str[1].split("\\.");
                        if (str.length == 2) {
                            int tmp = Integer.parseInt(str[0]);
                            if (tmp > index) {
                                index = tmp;
                            }
                        }
                    }
                }
                file = new File(dir, time + "-" + index + ".log");
                if (file.exists() && file.length() > 1024 * 2) {
                    index++;
                    file = new File(dir, time + "-" + index + ".log");
                }
            }
        } else {
            file = new File(dir, time + ".log");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            if (file.length() > 0) {
                fos.write("\r\n".getBytes());
            }
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String currentTime = dateFormat.format(date);
            fos.write(currentTime.getBytes());
            fos.write("\r\n".getBytes());
            fos.write(log.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void v(String msg) {
        print("v", msg);
    }

    public static void v(String msg, String path) {
        saveLog(path, msg);
        v(msg);
    }

    /**
     * 调试等级的打印并保存
     *
     * @param msg  日志内容
     * @param path 日志保存路径
     */
    public static void d(String msg, String path) {
        saveLog(path, msg);
        d(msg);
    }

    /**
     * 调试等级的打印
     *
     * @param msg 日志内容
     */
    public static void d(String msg) {
        print("d", msg);
    }

    /**
     * 警告等级的打印并保存
     *
     * @param msg  日志内容
     * @param path 日志保存路径
     */
    public static void i(String msg, String path) {
        saveLog(path, msg);
        i(msg);
    }

    /**
     * 警告等级的打印
     *
     * @param msg 日志内容
     */
    public static void i(String msg) {
        print("i", msg);
    }

    /**
     * 警告等级的打印
     *
     * @param msg 日志内容
     */
    public static void w(String msg) {
        print("w", msg);
    }

    /**
     * 警告等级的打印并保存
     *
     * @param msg  日志内容
     * @param path 日志保存路径
     */
    public static void w(String msg, String path) {
        saveLog(path, msg);
        w(msg);
    }

    /**
     * 错误等级的打印并保存
     *
     * @param msg  日志内容
     * @param path 日志保存路径
     */
    public static void e(String msg, String path) {
        saveLog(path, msg);
        e(msg);
    }


    /**
     * 错误等级的打印
     *
     * @param msg 日志内容
     */
    public static void e(String msg) {
        print("e", msg);
    }

    private static void print(String mtd, String msg) {
        if (!IS_DEBUG) {
            return;
        }
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String className = elements[elements.length - 1].getClassName();
        String[] tmp = className.split("\\.");
        className = NULL;
        for (String str : tmp) {
            className += str.charAt(0) + ".";
        }
        String methodName = elements[elements.length - 1].getMethodName().substring(0);
        if (cls != null) {
            try {
                Method method = cls.getDeclaredMethod(mtd, String.class, String.class);
                method.invoke(cls, className + "-" + "-" + methodName + "()", msg);
            } catch (Exception e) {
            }
        } else {
            if ("e".equals(mtd)) {
                System.err.println(className + "-" + methodName + "()  " + msg);
            } else {
                System.out.println(className + "-" + methodName + "()  " + msg);
            }
        }
    }

    private static class DirFilter implements FilenameFilter {
        private String regex;

        public DirFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(regex);
        }
    }
}

