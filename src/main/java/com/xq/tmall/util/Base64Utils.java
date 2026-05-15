package com.xq.tmall.util;

import java.util.Base64;

public class Base64Utils {

    // 将 byte[] 转换为 Base64 字符串
    public static String encode(byte[] data) {
        return data != null ? Base64.getEncoder().encodeToString(data) : null;
    }

    // 将 Base64 字符串转换为 byte[]
    public static byte[] decode(String base64Data) {
        return base64Data != null ? Base64.getDecoder().decode(base64Data) : null;
    }
}
