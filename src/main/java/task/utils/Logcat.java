package task.utils;

import java.io.*;
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
    private static final String FORMAT_TIME = "yyyy/MM/dd HH:mm:ss";
    private static boolean sIsDEBUG = true;
    private static Class<?> sCls;
    private static volatile Method sMethod = null;
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat(FORMAT_TIME);

    static {
        try {
            sCls = Class.forName("android.util.Log");
        } catch (Exception e) {
            sCls = null;
        }
    }

    public static void setsIsDEBUG(boolean sIsDEBUG) {
        Logcat.sIsDEBUG = sIsDEBUG;
    }

    private Logcat() {
    }


    /**
     * 保存日志
     *
     * @param pathFile 保存文件
     * @param log      日志内容
     */
    public synchronized static void saveLog(String pathFile, String log) {
        if (pathFile == null || log == null) {
            return;
        }

        File saveFile = new File(pathFile);

        boolean exists = saveFile.exists();
        if (!exists) {
            exists = saveFile.getParentFile().mkdirs();
        }

        if (exists) {
            try (FileOutputStream fos = new FileOutputStream(saveFile, true)) {
                Date date = new Date();
                String currentTime = sDateFormat.format(date);
                fos.write(currentTime.getBytes());
                fos.write("\r\n".getBytes());
                fos.write(log.getBytes());
                fos.write("\r\n".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
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
        if (!sIsDEBUG) {
            return;
        }
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement element = elements[3];
        String className = element.getClassName();
        String methodName = element.getMethodName();
        String[] tmp = className.split("\\.");
        if (sCls != null) {
            try {
                if (sMethod == null) {
                    sMethod = sCls.getDeclaredMethod(mtd, String.class, String.class);
                }
                sMethod.invoke(sCls, mtd, tmp[tmp.length - 1] + "-" + methodName + "()");
                sMethod.invoke(sCls, mtd, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if ("e".equals(mtd)) {
                System.err.println(tmp[tmp.length - 1] + "-" + methodName + "()  " + msg);
            } else {
                System.out.println(tmp[tmp.length - 1] + "-" + methodName + "()  " + msg);
            }
        }
    }

//    private static class DirFilter implements FilenameFilter {
//        private String regex;
//
//        public DirFilter(String regex) {
//            this.regex = regex;
//        }
//
//        @Override
//        public boolean accept(File dir, String name) {
//            return name.startsWith(regex);
//        }
//    }
}

