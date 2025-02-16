package com.llf.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class UUIDUtil {

    public static final String allChar = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String letterChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String numberChar = "0123456789";

    public static String[] chars =
            new String[] {
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
                    "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
            };

    /**
     * 用于生成8位唯一标识字符串
     */
    public static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 36]);
        }
        return shortBuffer.toString();
    }

    /**
     * 生成指定长度纯数字唯一标识字符串
     *
     * @param length
     * @return
     */
    public static String generatePureNumberUuid(int length) {
        StringBuffer shortBuffer = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            shortBuffer.append(numberChar.charAt(random.nextInt(10)));
        }
        return shortBuffer.toString();
    }

    /**
     * 由大小写字母、数字组成的随机字符串
     *
     * @param length
     * @return
     */
    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return sb.toString();
    }

    /**
     * 由大小写字母组成的随机字符串
     *
     * @param length
     * @return
     */
    public static String generateMixString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(letterChar.charAt(random.nextInt(letterChar.length())));
        }
        return sb.toString();
    }

    /**
     * 由小字字母组成的随机字符串
     *
     * @param length
     * @return
     */
    public static String generateLowerString(int length) {
        return generateMixString(length).toLowerCase();
    }

    /**
     * 由大写字母组成的随机字符串
     *
     * @param length
     * @return
     */
    public static String generateUpperString(int length) {
        return generateMixString(length).toUpperCase();
    }

    /**
     * 产生指字个数的0组成的字符串
     *
     * @param length
     * @return
     */
    public static String generateZeroString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 将数字转化成指字长度的字符串
     *
     * @param num
     * @param fixdlenth
     * @return
     */
    public static String toFixdLengthString(long num, int fixdlenth) {
        StringBuffer sb = new StringBuffer();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(generateZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth + "的字符串发生异常!");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 将数字转化成指字长度的字符串
     *
     * @param num
     * @param fixdlenth
     * @return
     */
    public static String toFixdLengthString(int num, int fixdlenth) {
        StringBuffer sb = new StringBuffer();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(generateZeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth + "的字符串发生异常!");
        }
        sb.append(strNum);
        return sb.toString();
    }

    // 生成订单编号，时间戳+后8位随机字符串
    public static String getOrderNo() {
        String orderNo = "";
        String sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        orderNo = sdf + generateShortUuid();
        return orderNo;
    }

    /**
     * 这个方法只支持最大长度为32的随机字符串,如要支持更大长度的，可以适当修改此方法，如前面补、后面补，或者多个uuid相连接
     *
     * @param length
     * @return
     */
    private static String toFixedLengthStringByUUID(int length) {

        // 也可以通过UUID来随机生成
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "").substring(0, length);
    }
}
