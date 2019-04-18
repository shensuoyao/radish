package org.sam.shen.scheduing.cluster.utils;

import java.util.Arrays;

/**
 * @author clock
 * @date 2019/4/18 上午11:05
 */
public class ByteUtils {


    /**
     * 将int数据转成byte数组
     * @author clock
     * @date 2019/4/18 下午2:33
     * @param n int数据
     * @return 转化后的byte数组
     */
    public static byte[] intToByteArray(int n) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (n >> (24 - i * 8));
        }
        return result;
    }

    /**
     * 将byte数组转int
     * @author clock
     * @date 2019/4/18 下午2:32
     * @param b byte数组
     * @return 转化后的int数据
     */
    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    /**
     * 合并2个byte数组
     * @author clock
     * @date 2019/4/18 下午2:27
     * @param first 第一个byte数组
     * @param second 第二个byte数组
     * @return 合并之后的数组
     */
    public static byte[] mergeByteArray(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
