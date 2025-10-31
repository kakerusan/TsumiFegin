package fun.hatsumi.tsumifeign.codec;

import java.lang.reflect.Type;

/**
 * 解码器接口
 * 负责将字节数组解码为 Java 对象
 *
 * @author Kakeru
 */
public interface Decoder {

    /**
     * 将字节数组解码为对象
     *
     * @param bytes 字节数组
     * @param type 目标类型
     * @return 解码后的对象
     */
    Object decode(byte[] bytes, Type type);
}
