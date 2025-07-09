package com.yunji.hygiene.util;

/**
 * @author : peter-zhu
 * @date : 2025/6/25 23:53
 * @description : TODO
 **/
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class NettyUtil {

    // 将 16 进制字符串转为 byte[]
    public static byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex 字符串非法");
        }
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            int index = i * 2;
            result[i] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
        }
        return result;
    }

    // 发送 hex 数据
    public static void sendHex(ChannelHandlerContext ctx, String hexStr) {
        byte[] bytes = hexStringToBytes(hexStr);
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        ctx.writeAndFlush(buffer);
    }
}

