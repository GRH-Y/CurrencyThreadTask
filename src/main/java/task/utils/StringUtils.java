package com.yyz.android.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * 判断是否为null或空字符串
     *
     * @param str
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否不为null或不是空字符串
     *
     * @param str
     * @return boolean
     */
    public static boolean isNotEmpty(String str) {
        if (str == null || str.trim().equals(""))
            return false;
        return true;
    }

    /**
     * 判断整型
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        return str.matches("[\\d]+");
    }

    /**
     * //判断小数，与判断整型的区别在与d后面的小数点（红色）
     *
     * @param str
     * @return
     */
    public static boolean isFloatNumber(String str) {
        return str.matches("[\\d.]+");
    }


    /**
     * @param phoneNumber 手机号码
     * @return 是否合法
     * @Description: 验证手机号码是否合法
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        String reg = "1[3,4,5,7,8]{1}\\d{9}";
        return phoneNumber.matches(reg);
    }


    /**
     * @param password 密码
     * @return 是否合法
     * @Description: 合法密码，6位以上的数字和字母
     */
    public static boolean isPassword(String password) {
        Pattern p = Pattern.compile("^([0-9]|[a-zA-Z]){6,}$");
        Matcher m = p.matcher(password);
        return m.matches();
    }

    /**
     * @param activation 激活码
     * @return 是否合法
     * @Description: 验证激活码是否正确
     */
    public static boolean isActivation(String activation) {
        Pattern p = Pattern.compile("^[0-9]{7}$");
        Matcher m = p.matcher(activation);
        return m.matches();
    }

    /**
     * @param mileage 里程数
     * @return 是否合法
     * @Description: 验证里程数是否正确
     */
    public static boolean isMileage(String mileage) {
        Pattern p = Pattern.compile("^[0-9]{1,9}");
        Matcher m = p.matcher(mileage);
        return m.matches();
    }

    /**
     * @param vcode 短信验证码
     * @return 是否合法
     * @Description: 验证短信验证码是否正确
     */
    public static boolean isSMSVcode(String vcode) {
        Pattern p = Pattern.compile("^[0-9]{4}$");
        Matcher m = p.matcher(vcode);
        return m.matches();
    }


    /**
     * 判断车牌号码是否正确
     */
    public static boolean isVehicleNo(String vehicleNo) {
        boolean flag = false;
        String regEx = "^[\u4e00-\u9fa5][a-zA-Z0-9]{6}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(vehicleNo);
        if (!matcher.matches()) {
//				ExceptionRemind.msg ="输入的车牌号不符合规范";
        } else {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断是否为教练车车牌
     */
    public static boolean isCoachCarsPlate(String plate) {
        String regEx = "^[\u4e00-\u9fa5][a-zA-Z0-9]{5}[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(plate);
        return matcher.matches();
    }

    /**
     * 校验车牌号是否正确
     * 校验规：	1.车牌号满5位不能输入
     * 2.如果少于5位则保存时提示“请输入完整的车牌号
     *
     * @param plateNum 车牌号
     * @return true = 正确; false = 不正确
     * @author swallow
     * @createTime 2015/7/2
     * @lastModify 2015/7/2
     */
    public static boolean isPlateNumRight(String plateNum) {
        return !(plateNum.length() < 7);
    }


    /**
     * <判断是否为车架号>
     *
     * @param rackNo
     * @return
     */
    public static boolean isRackNo(String rackNo) {
        String reg = "^[0-9a-zA-Z]{17}$";
        return rackNo.matches(reg);
    }

    /**
     * <判断是否为发动机号>
     *
     * @param enginNo
     * @return
     */
    public static boolean isEnginNo(String enginNo) {
        String reg = "^[0-9a-zA-Z]{6,}$";
        return enginNo.matches(reg);
    }

    /**
     * 去掉空格换行符
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 是否为英文
     *
     * @param str
     * @return
     */
    public static boolean isEnglish(String str) {
        String reg = "^[a-zA-Z]*";
        return str.matches(reg);
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 判定输入汉字
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 检测String是否全是中文
     *
     * @param str 要判断的字符串
     * @return true则全是中文
     */
    public static boolean checkStringChinese(String str) {
        boolean res = false;
        if (str != null && str.length() > 0) {
            char[] cTemp = str.toCharArray();
            for (int i = 0; i < str.length(); i++) {
                if (!isChinese(cTemp[i])) {
                    res = false;
                    break;
                }
                res = true;
            }
        }
        return res;
    }

    /**
     * 十六进制转换字符串
     *
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] charArray = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(charArray[2 * i]) * 16;
            n += str.indexOf(charArray[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @return String 每个Byte值之间空格分隔
     */
    public static String byteToHexStr(byte[] b) {
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            String tmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((tmp.length() == 1) ? "0" + tmp : tmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @return byte[]
     */
    public static byte[] hexStrToBytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }
}
