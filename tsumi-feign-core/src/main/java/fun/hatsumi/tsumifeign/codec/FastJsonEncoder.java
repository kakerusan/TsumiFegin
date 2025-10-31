package fun.hatsumi.tsumifeign.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.nio.charset.StandardCharsets;

/**
 * FastJSON 编码器实现
 *
 * @author Kakeru
 */
public class FastJsonEncoder implements Encoder {

    @Override
    public byte[] encode(Object object) {
        if (object == null) {
            return new byte[0];
        }

        if (object instanceof String) {
            return ((String) object).getBytes(StandardCharsets.UTF_8);
        }

        if (object instanceof byte[]) {
            return (byte[]) object;
        }

        // 使用 FastJSON2 将对象转换为 JSON 字节数组
        return JSON.toJSONBytes(object, JSONWriter.Feature.WriteMapNullValue);
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }
}
