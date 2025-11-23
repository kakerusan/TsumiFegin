package fun.hatsumi.tsumifeign.codec;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * FastJSON 解码器实现
 *
 * @author Kakeru
 */
public class FastJsonDecoder implements Decoder {

    @Override
    public Object decode(byte[] bytes, Type type) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        // 如果目标类型是 String，直接返回字符串
        if (type == String.class) {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        // 如果目标类型是 byte[]，直接返回字节数组
        if (type == byte[].class) {
            return bytes;
        }

        // 如果目标类型是 void 或 Void，返回 null
        if (type == void.class || type == Void.class) {
            return null;
        }

        // 使用 FastJSON2 解析 JSON
        return JSON.parseObject(bytes, type);
    }
}
