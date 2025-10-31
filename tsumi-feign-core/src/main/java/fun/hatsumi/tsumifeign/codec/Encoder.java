package fun.hatsumi.tsumifeign.codec;

/**
 * 编码器接口
 * 负责将 Java 对象编码为字节数组
 *
 * @author Kakeru
 */
public interface Encoder {

    /**
     * 将对象编码为字节数组
     *
     * @param object 待编码对象
     * @return 编码后的字节数组
     */
    byte[] encode(Object object);

    /**
     * 获取 Content-Type
     */
    String getContentType();
}
