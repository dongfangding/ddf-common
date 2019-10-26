package com.ddf.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 */
public class MD5Util {
    protected static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final String SALT = "abcdefghijklmnopqrstuvwxy";


    protected static MessageDigest messageDigest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(MD5Util.class.getName() + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 转换字节数组为16进制字串
     *
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * J 转换byte到16进制
     *
     * @param b
     * @return
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + "" + hexDigits[d2];
    }

    /**
     * J 编码
     *
     * @param origin 需加密的字符串
     * @return String 加密后的字符串
     */
    public static String encode(String origin) {
        return byteArrayToHexString(messageDigest.digest(origin.getBytes()));
    }

    /**
     * MessageDigest 为 JDK 提供的加密类
     *
     * @param bytes 需加密的字节数组
     * @return String 加密后的字符串
     */
    public static String encode(byte[] bytes) {
        return byteArrayToHexString(messageDigest.digest(bytes));
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     * MessageDigest 为 JDK 提供的加密类
     *
     * @param @param  binaryData
     * @param @return
     * @return byte[]
     * @throws
     * @Title: MD5EncodeBytes
     * @Description:
     */
    public static byte[] encodeToByte(byte[] binaryData) {
        messageDigest.update(binaryData);
        return messageDigest.digest();
    }

    /**
     * 通过特定编码格式加密字符串
     *
     * @param origin      需加密的字符串
     * @param charsetName 编码格式
     * @return String 加密后的字符串
     */
    public static String encode(String origin, String charsetName) {
        origin = origin.trim();
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetName)));
        } catch (Exception ex) {
        }
        return resultString;
    }

    public static String getFileMD5String(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        messageDigest.update(byteBuffer);
        return bufferToHex(messageDigest.digest());
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    /**
     * MD5加盐
     * @param string
     * @param slat
     * @return
     */
    public static String encodeSalt(String string, String slat) {
        byte[] bytes = messageDigest.digest((string + slat).getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String temp = Integer.toHexString(b & 0xff);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            result.append(temp);
        }
        return result.toString();
    }

    /**
     * MD5加盐
     * @param string
     * @return
     */
    public static String encodeSalt(String string) {
        return encodeSalt(string, SALT);
    }
}