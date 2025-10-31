package fun.hatsumi.tsumifeign.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * HTTP 响应封装
 *
 * @author Kakeru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    /**
     * 响应状态码
     */
    private int status;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应体
     */
    private byte[] body;

    /**
     * 获取响应体字符串
     */
    public String getBodyAsString() {
        if (body == null) {
            return null;
        }
        return new String(body);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
