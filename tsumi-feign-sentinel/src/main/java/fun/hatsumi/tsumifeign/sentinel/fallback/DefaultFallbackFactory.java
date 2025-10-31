package fun.hatsumi.tsumifeign.sentinel.fallback;

import fun.hatsumi.tsumifeign.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 Fallback 工厂
 * 返回统一的降级响应
 *
 * @author kakeru
 */
@Slf4j
public class DefaultFallbackFactory implements FallbackFactory<Response> {

    @Override
    public Response create(Throwable cause) {
        log.warn("Service degraded due to: {}", cause.getMessage());
        
        return Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .body("{\"error\":\"Service temporarily unavailable\"}".getBytes())
                .build();
    }
}
