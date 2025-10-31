package fun.hatsumi.tsumifeign.client;

import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;

import java.io.IOException;

/**
 * Feign 客户端接口
 *
 * @author Kakeru
 */
public interface FeignClient {

    /**
     * 执行 HTTP 请求
     *
     * @param requestTemplate 请求模板
     * @return HTTP 响应
     * @throws IOException IO 异常
     */
    Response execute(RequestTemplate requestTemplate) throws IOException;
}
