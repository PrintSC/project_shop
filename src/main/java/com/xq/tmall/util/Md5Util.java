package com.xq.tmall.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

    /**
     * 对给定的字符串进行MD5加密。
     *
     * @param password 要加密的密码字符串。
     * @return 加密后的MD5哈希值，以32位十六进制字符串表示。
     */
    public static String encryptToMD5(String password) {
        try {
            // 获取MessageDigest实例，指定算法为MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将输入的字符串转换为字节数组并计算其摘要
            byte[] messageDigest = md.digest(password.getBytes());

            // 创建BigInteger对象，使用10进制和messageDigest构造函数
            // 使用BigInteger来处理可能的符号问题（确保结果是正数）
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不被支持", e);
        }
    }
}