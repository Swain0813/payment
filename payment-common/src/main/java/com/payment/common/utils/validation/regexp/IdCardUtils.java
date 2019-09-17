package com.payment.common.utils.validation.regexp;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @version v1.0.0
 * @classDesc: 类描述: 中华人民共和国《公民身份号码》工具类
 * @createTime 2018年3月14日 下午8:37:53
 * @copyright: 上海众哈网络技术有限公司
 */
public class IdCardUtils {

    /**
     * 中国公民身份证号码最小长度。
     */
    private static final int CHINA_ID_MIN_LENGTH = 15;

    /**
     * 中国公民身份证号码最大长度。
     */
    private static final int CHINA_ID_MAX_LENGTH = 18;

    /**
     * 每位加权因子
     */
    private static final int power[] = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 最低年限
     */
    private static final int MIN = 1930;

    private static Map<String, String> CITY_CODES = new HashMap<String, String>();

    /**
     * 台湾身份首字母对应数字
     */
    private static Map<String, Integer> TW_F_CODE = new HashMap<String, Integer>();

    /**
     * 香港身份首字母对应数字
     */
    private static Map<String, Integer> HK_F_CODE = new HashMap<String, Integer>();

    static {
        CITY_CODES.put("11", "北京");
        CITY_CODES.put("12", "天津");
        CITY_CODES.put("13", "河北");
        CITY_CODES.put("14", "山西");
        CITY_CODES.put("15", "内蒙古");
        CITY_CODES.put("21", "辽宁");
        CITY_CODES.put("22", "吉林");
        CITY_CODES.put("23", "黑龙江");
        CITY_CODES.put("31", "上海");
        CITY_CODES.put("32", "江苏");
        CITY_CODES.put("33", "浙江");
        CITY_CODES.put("34", "安徽");
        CITY_CODES.put("35", "福建");
        CITY_CODES.put("36", "江西");
        CITY_CODES.put("37", "山东");
        CITY_CODES.put("41", "河南");
        CITY_CODES.put("42", "湖北");
        CITY_CODES.put("43", "湖南");
        CITY_CODES.put("44", "广东");
        CITY_CODES.put("45", "广西");
        CITY_CODES.put("46", "海南");
        CITY_CODES.put("50", "重庆");
        CITY_CODES.put("51", "四川");
        CITY_CODES.put("52", "贵州");
        CITY_CODES.put("53", "云南");
        CITY_CODES.put("54", "西藏");
        CITY_CODES.put("61", "陕西");
        CITY_CODES.put("62", "甘肃");
        CITY_CODES.put("63", "青海");
        CITY_CODES.put("64", "宁夏");
        CITY_CODES.put("65", "新疆");
        CITY_CODES.put("71", "台湾");
        CITY_CODES.put("81", "香港");
        CITY_CODES.put("82", "澳门");
        CITY_CODES.put("91", "国外");
        TW_F_CODE.put("A", 10);
        TW_F_CODE.put("B", 11);
        TW_F_CODE.put("C", 12);
        TW_F_CODE.put("D", 13);
        TW_F_CODE.put("E", 14);
        TW_F_CODE.put("F", 15);
        TW_F_CODE.put("G", 16);
        TW_F_CODE.put("H", 17);
        TW_F_CODE.put("J", 18);
        TW_F_CODE.put("K", 19);
        TW_F_CODE.put("L", 20);
        TW_F_CODE.put("M", 21);
        TW_F_CODE.put("N", 22);
        TW_F_CODE.put("P", 23);
        TW_F_CODE.put("Q", 24);
        TW_F_CODE.put("R", 25);
        TW_F_CODE.put("S", 26);
        TW_F_CODE.put("T", 27);
        TW_F_CODE.put("U", 28);
        TW_F_CODE.put("V", 29);
        TW_F_CODE.put("X", 30);
        TW_F_CODE.put("Y", 31);
        TW_F_CODE.put("W", 32);
        TW_F_CODE.put("Z", 33);
        TW_F_CODE.put("I", 34);
        TW_F_CODE.put("O", 35);
        HK_F_CODE.put("A", 1);
        HK_F_CODE.put("B", 2);
        HK_F_CODE.put("C", 3);
        HK_F_CODE.put("R", 18);
        HK_F_CODE.put("U", 21);
        HK_F_CODE.put("Z", 26);
        HK_F_CODE.put("X", 24);
        HK_F_CODE.put("W", 23);
        HK_F_CODE.put("O", 15);
        HK_F_CODE.put("N", 14);
    }

    /**
     * 工具类，私有构造方法。
     */
    private IdCardUtils() {

    }

    /**
     * 返回<tt>《公民身份号码》</tt>是否有效。
     *
     * @param s <tt>《公民身份号码》</tt> 字符串。
     * @return <tt>true</tt> 表示有效，<tt>false</tt> 表示无效。
     * @author JoeHe
     * @version 1.0.0
     * @since 2017年5月8日
     */
    public static boolean isValid(String id) {

        if ((id == null) || (id.length() == 0)) {
            return false;
        }
        String card = id.trim();
        if (validateIdCard18(card)) {
            return true;
        }
        if (validateIdCard15(card)) {
            return true;
        }

        // // 港澳台
        // String[] cardval = validateIdCard10(card);
        // if (cardval != null) {
        // if (cardval[2].equals("true")) {
        // return true;
        // }
        // }
        return false;
    }

    /**
     * 将15位身份证号码转换为18位
     *
     * @param idCard 15位身份编码
     * @return 18位身份编码
     */
    private static String conver15CardTo18(String idCard) {

        String idCard18 = "";
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (isNum(idCard)) {
            // 获取出生年月日
            String birthday = idCard.substring(6, 12);
            Date birthDate = null;
            try {
                birthDate = new SimpleDateFormat("yyMMdd").parse(birthday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            if (birthDate != null) {
                cal.setTime(birthDate);
            }
            // 获取出生年(完全表现形式,如：2010)
            String sYear = String.valueOf(cal.get(Calendar.YEAR));
            idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
            // 转换字符数组
            char[] cArr = idCard18.toCharArray();
            if (cArr != null) {
                int[] iCard = converCharToInt(cArr);
                int iSum17 = getPowerSum(iCard);
                // 获取校验位
                String sVal = getCheckCode18(iSum17);
                if (sVal.length() > 0) {
                    idCard18 += sVal;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
        return idCard18;
    }

    /**
     * 验证18位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    private static boolean validateIdCard18(String idCard) {

        boolean bTrue = false;
        if (idCard.length() == CHINA_ID_MAX_LENGTH) {
            // 前17位
            String code17 = idCard.substring(0, 17);
            // 第18位
            String code18 = idCard.substring(17, CHINA_ID_MAX_LENGTH);
            if (isNum(code17)) {
                char[] cArr = code17.toCharArray();
                if (cArr != null) {
                    int[] iCard = converCharToInt(cArr);
                    int iSum17 = getPowerSum(iCard);
                    // 获取校验位
                    String val = getCheckCode18(iSum17);
                    if (val.length() > 0) {
                        if (val.equalsIgnoreCase(code18)) {
                            bTrue = true;
                        }
                    }
                }
            }
        }
        return bTrue;
    }

    /**
     * 验证15位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    private static boolean validateIdCard15(String idCard) {

        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return false;
        }
        if (isNum(idCard)) {
            String proCode = idCard.substring(0, 2);
            if (CITY_CODES.get(proCode) == null) {
                return false;
            }
            String birthCode = idCard.substring(6, 12);
            Date birthDate = null;
            try {
                birthDate = new SimpleDateFormat("yy").parse(birthCode.substring(0, 2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            if (birthDate != null) {
                cal.setTime(birthDate);
            }
            if (!valiDate(cal.get(Calendar.YEAR), Integer.valueOf(birthCode.substring(2, 4)),
                    Integer.valueOf(birthCode.substring(4, 6)))) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * 验证10位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 身份证信息数组
     * <p>
     * [0] - 台湾、澳门、香港 [1] - 性别(男M,女F,未知N) [2] - 是否合法(合法true,不合法false)
     * 若不是身份证件号码则返回null
     * </p>
     */
    @SuppressWarnings("unused")
    private static String[] validateIdCard10(String idCard) {

        String[] info = new String[3];
        String card = idCard.replaceAll("[\\(|\\)]", "");
        if ((card.length() != 8) && (card.length() != 9) && (idCard.length() != 10)) {
            return null;
        }
        if (idCard.matches("^[a-zA-Z][0-9]{9}$")) { // 台湾
            info[0] = "台湾";
            System.out.println("11111");
            String char2 = idCard.substring(1, 2);
            if (char2.equals("1")) {
                info[1] = "M";
                System.out.println("MMMMMMM");
            } else if (char2.equals("2")) {
                info[1] = "F";
                System.out.println("FFFFFFF");
            } else {
                info[1] = "N";
                info[2] = "false";
                System.out.println("NNNN");
                return info;
            }
            info[2] = validateTWCard(idCard) ? "true" : "false";
        } else if (idCard.matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$")) { // 澳门
            info[0] = "澳门";
            info[1] = "N";
        } else if (idCard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) { // 香港
            info[0] = "香港";
            info[1] = "N";
            info[2] = validateHKCard(idCard) ? "true" : "false";
        } else {
            return null;
        }
        return info;
    }

    /**
     * 验证台湾身份证号码
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    private static boolean validateTWCard(String idCard) {

        String start = idCard.substring(0, 1);
        String mid = idCard.substring(1, 9);
        String end = idCard.substring(9, 10);
        Integer iStart = TW_F_CODE.get(start);
        Integer sum = (iStart / 10) + ((iStart % 10) * 9);
        char[] chars = mid.toCharArray();
        Integer iflag = 8;
        for (char c : chars) {
            sum = sum + (Integer.valueOf(c + "") * iflag);
            iflag--;
        }
        return ((sum % 10) == 0 ? 0 : (10 - (sum % 10))) == Integer.valueOf(end) ? true : false;
    }

    /**
     * 验证香港身份证号码(存在Bug，部份特殊身份证无法检查)
     * <p>
     * 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35
     * 最后一位校验码为0-9的数字加上字符"A"，"A"代表10
     * </p>
     * <p>
     * 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
     * </p>
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    private static boolean validateHKCard(String idCard) {

        String card = idCard.replaceAll("[\\(|\\)]", "");
        Integer sum = 0;
        if (card.length() == 9) {
            sum = ((Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 9)
                    + ((Integer.valueOf(card.substring(1, 2).toUpperCase().toCharArray()[0]) - 55) * 8);
            card = card.substring(1, 9);
        } else {
            sum = 522 + ((Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 8);
        }
        String mid = card.substring(1, 7);
        String end = card.substring(7, 8);
        char[] chars = mid.toCharArray();
        Integer iflag = 7;
        for (char c : chars) {
            sum = sum + (Integer.valueOf(c + "") * iflag);
            iflag--;
        }
        if (end.toUpperCase().equals("A")) {
            sum = sum + 10;
        } else {
            sum = sum + Integer.valueOf(end);
        }
        return ((sum % 11) == 0) ? true : false;
    }

    /**
     * 将字符数组转换成数字数组
     *
     * @param ca 字符数组
     * @return 数字数组
     */
    private static int[] converCharToInt(char[] ca) {

        int len = ca.length;
        int[] iArr = new int[len];
        try {
            for (int i = 0; i < len; i++) {
                iArr[i] = Integer.parseInt(String.valueOf(ca[i]));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return iArr;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param iArr
     * @return 身份证编码。
     */
    private static int getPowerSum(int[] iArr) {

        int iSum = 0;
        if (power.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                for (int j = 0; j < power.length; j++) {
                    if (i == j) {
                        iSum = iSum + (iArr[i] * power[j]);
                    }
                }
            }
        }
        return iSum;
    }

    /**
     * 将power和值与11取模获得余数进行校验码判断
     *
     * @param iSum
     * @return 校验位
     */
    private static String getCheckCode18(int iSum) {

        String sCode = "";
        switch (iSum % 11) {
            case 10:
                sCode = "2";
                break;
            case 9:
                sCode = "3";
                break;
            case 8:
                sCode = "4";
                break;
            case 7:
                sCode = "5";
                break;
            case 6:
                sCode = "6";
                break;
            case 5:
                sCode = "7";
                break;
            case 4:
                sCode = "8";
                break;
            case 3:
                sCode = "9";
                break;
            case 2:
                sCode = "x";
                break;
            case 1:
                sCode = "0";
                break;
            case 0:
                sCode = "1";
                break;
        }
        return sCode;
    }

    /**
     * 根据身份编号获取年龄
     *
     * @param idCard 身份编号
     * @return 年龄
     */
    public static int getAgeByIdCard(String idCard) {

        int iAge = 0;
        if (idCard.length() == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        String year = idCard.substring(6, 10);
        String month = idCard.substring(10, 12);
        String day = idCard.substring(12, 14);
        Calendar cal = Calendar.getInstance();
        int iCurrYear = cal.get(Calendar.YEAR);
        iAge = iCurrYear - Integer.valueOf(year);
        if (iAge == 0) {
            return 0;
        }
        // 计算年龄精确到天
        if (((Integer.valueOf(month) * 100) + Integer.valueOf(day)) >= (((cal.get(Calendar.MONTH) + 1) * 100)
                + cal.get(Calendar.DAY_OF_MONTH))) {
            return iAge - 1;
        }
        return iAge;
    }

    /**
     * 根据身份编号获取出生日期
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static String getBirthByIdCard(String idCard) {

        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return idCard.substring(6, 14);
    }

    /**
     * 根据身份编号获取生日年
     *
     * @param idCard 身份编号
     * @return 生日(yyyy)
     */
    @SuppressWarnings("unused")
    private static Short getYearByIdCard(String idCard) {

        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(6, 10));
    }

    /**
     * 根据身份编号获取生日月
     *
     * @param idCard 身份编号
     * @return 生日(MM)
     */
    @SuppressWarnings("unused")
    private static Short getMonthByIdCard(String idCard) {

        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(10, 12));
    }

    /**
     * 根据身份编号获取生日天
     *
     * @param idCard 身份编号
     * @return 生日(dd)
     */
    @SuppressWarnings("unused")
    private static Short getDateByIdCard(String idCard) {

        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(12, 14));
    }

    /**
     * 根据身份编号获取性别
     *
     * @param idCard 身份编号
     * @return 性别(0 - 男 ， 1 - 女 ， N - 未知)
     */
    public static int getGenderByIdCard(String idCard) {

        int sGender = 2;
        if (idCard.length() == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        String sCardNum = idCard.substring(16, 17);
        if ((Integer.parseInt(sCardNum) % 2) != 0) {
            sGender = 0;
        } else {
            sGender = 1;
        }
        return sGender;
    }

    /**
     * 根据身份编号获取户籍省份
     *
     * @param idCard 身份编码
     * @return 省级编码。
     */
    public static String getProvinceByIdCard(String idCard) {

        int len = idCard.length();
        String sProvince = null;
        String sProvinNum = "";
        if ((len == CHINA_ID_MIN_LENGTH) || (len == CHINA_ID_MAX_LENGTH)) {
            sProvinNum = idCard.substring(0, 2);
        }
        sProvince = CITY_CODES.get(sProvinNum);
        return sProvince;
    }

    /**
     * 数字验证
     *
     * @param val
     * @return 提取的数字。
     */
    private static boolean isNum(String val) {

        return (val == null) || "".equals(val) ? false : val.matches("^[0-9]*$");
    }

    /**
     * 验证小于当前日期 是否有效
     *
     * @param iYear  待验证日期(年)
     * @param iMonth 待验证日期(月 1-12)
     * @param iDate  待验证日期(日)
     * @return 是否有效
     */
    private static boolean valiDate(int iYear, int iMonth, int iDate) {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int datePerMonth;
        if ((iYear < MIN) || (iYear >= year)) {
            return false;
        }
        if ((iMonth < 1) || (iMonth > 12)) {
            return false;
        }
        switch (iMonth) {
            case 4:
            case 6:
            case 9:
            case 11:
                datePerMonth = 30;
                break;
            case 2:
                boolean dm = ((((iYear % 4) == 0) && ((iYear % 100) != 0)) || ((iYear % 400) == 0))
                        && ((iYear > MIN) && (iYear < year));
                datePerMonth = dm ? 29 : 28;
                break;
            default:
                datePerMonth = 31;
        }
        return (iDate >= 1) && (iDate <= datePerMonth);
    }
}