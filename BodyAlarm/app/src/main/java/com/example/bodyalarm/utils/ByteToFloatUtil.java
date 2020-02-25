package com.example.bodyalarm.utils;

/**
 * Created by Jorble on 2016/2/29.
 */
public class ByteToFloatUtil {
    /*
	 * 解析单数值型传感器数据
	 * 例如：0x01 0xFF -> 511
	 */
    public static float hBytesToFloat(byte[] b) {			//16进制byte转换为float
        int s = 0;
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        s = s * 256;
        if (b[1] >= 0) {
            s = s + b[1];
        } else {
            s = s + 256 + b[1];
        }
        float result = (float) s;
        return result;
    }

    /*
     * 解析双数据型传感器数据（温湿度）
     * 例如：0x01 0xFF ->51.1      （01 * 256） + 0xFF = 511,则湿度的实际值为51.1RH%
     *      0x00 0xFF ->25.5    （00 * 256） + 0xFF = 255,则温度的实际值为25.5℃
     */
    public static float[] getTemHumResult(byte[] b) {		//温湿度传感器
        float[] f = new float[2];
        for (int i = 0; i < b.length; i = i + 2) {
            byte[] bb = new byte[2];
            System.arraycopy(b, i, bb, 0, 2);
            float w = hBytesToFloat(bb);
            f[i / 2] = (float) (w / 10.0);
        }
        return f;
    }

}
