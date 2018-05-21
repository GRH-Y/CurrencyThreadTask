package task.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     *  转换时间格式
     * @param date yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDateTimeFromMilli(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm
     *
     * @param millisecond
     * @return
     */
    public static String getDateTimeFromMilli(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    public static long getDateStrToTimestamp(String dateStr) {
        //注意format的格式要与日期String的格式相匹配
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 当前时间是否是今天
     *
     * @return
     */
    public static boolean isToday(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        calendar.set(year, month, date, 23, 59, 59);
        long todayMaxTime = calendar.getTimeInMillis();
        calendar.set(year, month, date, 00, 00, 00);
        long todayMinTime = calendar.getTimeInMillis();
        return todayMaxTime >= timeMillis && timeMillis >= todayMinTime;
    }

    /**
     * 当前时间是否是昨天
     *
     * @param timeMillis 判断的时间
     * @return
     */
    public static boolean isYesterday(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        calendar.set(year, month, date - 1, 23, 59, 59);
        long yesterdayMaxTime = calendar.getTimeInMillis();
        calendar.set(year, month, date - 1, 00, 00, 00);
        return yesterdayMaxTime >= timeMillis && timeMillis >= calendar.getTimeInMillis();
    }

    /**
     * 当前时间是否是前天
     *
     * @param timeMillis 判断的时间
     * @return
     */
    public static boolean isDayBeforeYesterday(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        calendar.set(year, month, date - 2, 23, 59, 59);
        long threeDayMaxTime = calendar.getTimeInMillis();
        calendar.set(year, month, date - 2, 00, 00, 00);
        return threeDayMaxTime >= timeMillis && timeMillis >= calendar.getTimeInMillis();
    }

    /**
     * 获取小时分钟秒
     *
     * @param millisecond
     * @return
     */
    public static String getHourMinute(Long millisecond) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

}
