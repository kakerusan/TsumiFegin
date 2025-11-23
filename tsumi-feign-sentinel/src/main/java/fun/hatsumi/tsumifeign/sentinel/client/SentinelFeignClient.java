package fun.hatsumi.tsumifeign.sentinel.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import fun.hatsumi.tsumifeign.sentinel.fallback.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 支持 Sentinel 熔断降级的 Feign 客户端
 *
 * @author kakeru
 */
@Slf4j
public class SentinelFeignClient implements FeignClient {

    private final FeignClient delegate;
    private final FallbackFactory<?> fallbackFactory;
    private final String resourcePrefix;

    public SentinelFeignClient(FeignClient delegate, 
                               FallbackFactory<?> fallbackFactory,
                               String resourcePrefix) {
        this.delegate = delegate;
        this.fallbackFactory = fallbackFactory;
        this.resourcePrefix = resourcePrefix;
    }

    @Override
    public Response execute(RequestTemplate requestTemplate) throws IOException {
        // 构建 Sentinel 资源名称：服务名:HTTP方法:路径
        String resourceName = buildResourceName(requestTemplate);
        
        Entry entry = null;
        try {
            // 设置调用来源
            ContextUtil.enter(resourceName, "tsumi-feign");
            
            // 定义 Sentinel 资源
            entry = SphU.entry(resourceName);
            
            // 执行实际请求
            Response response = delegate.execute(requestTemplate);
            
            // 检查响应状态，如果是 5xx 错误，可能需要触发降级
            if (response.getStatus() >= 500) {
                log.warn("Server error detected for resource: {}, status: {}", 
                        resourceName, response.getStatus());
            }
            
            return response;
            
        } catch (BlockException e) {
            // 被 Sentinel 限流或熔断
            log.warn("Request blocked by Sentinel for resource: {}", resourceName, e);
            
            // 执行降级逻辑
            if (fallbackFactory != null) {
                return handleFallback(requestTemplate, e);
            }
            
            throw new IOException("Request blocked by Sentinel: " + e.getMessage(), e);
            
        } catch (IOException e) {
            // HTTP 请求异常
            log.error("HTTP request failed for resource: {}", resourceName, e);
            
            // 执行降级逻辑
            if (fallbackFactory != null) {
                return handleFallback(requestTemplate, e);
            }
            
            throw e;
            
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    /**
     * 构建资源名称
     */
    private String buildResourceName(RequestTemplate requestTemplate) {
        String url = requestTemplate.getUrl();
        String method = requestTemplate.getMethod();
        String path = requestTemplate.getPath();
        
        // 格式: prefix:服务名:HTTP方法:路径
        if (resourcePrefix != null && !resourcePrefix.isEmpty()) {
            return String.format("%s:%s:%s%s", resourcePrefix, url, method, path);
        }
        
        return String.format("%s:%s%s", url, method, path);
    }

    /**
     * 处理降级逻辑
     */
    private Response handleFallback(RequestTemplate requestTemplate, Throwable throwable) {
        log.info("Executing fallback for request: {} {}", 
                requestTemplate.getMethod(), requestTemplate.getPath());
        
        try {
            return fallbackFactory.create(throwable);
        } catch (Exception e) {
            log.error("Fallback execution failed", e);
            // 返回默认错误响应
            return Response.builder()
                    .status(503)
                    .reason("Service Unavailable - Fallback Failed")
                    .body(new byte[0])
                    .build();
        }
    }
}
