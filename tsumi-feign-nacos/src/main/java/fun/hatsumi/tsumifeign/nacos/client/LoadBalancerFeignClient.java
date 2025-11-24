package fun.hatsumi.tsumifeign.nacos.client;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.io.IOException;
import java.util.HashMap;

/**
 * 支持负载均衡的 Feign 客户端
 * 集成 Spring Cloud LoadBalancer 和 Nacos 服务发现
 *
 * @author kakeru
 */
@Slf4j
public class LoadBalancerFeignClient implements FeignClient {

    private final FeignClient delegate;
    private final LoadBalancerClient loadBalancerClient;

    public LoadBalancerFeignClient(FeignClient delegate, LoadBalancerClient loadBalancerClient) {
        this.delegate = delegate;
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public Response execute(RequestTemplate requestTemplate) throws IOException {
        String serviceName = requestTemplate.getUrl();

        // 如果 URL 为空或已经是完整 URL，直接使用原客户端
        if (serviceName == null || serviceName.isEmpty() || serviceName.startsWith("http://") || serviceName.startsWith("https://")) {
            log.debug("Bypassing load balancer for URL: {}", serviceName);
            return delegate.execute(requestTemplate);
        }

        // URL 是服务名，使用负载均衡
        log.debug("Using load balancer for service: {}", serviceName);

        try {
            // 从负载均衡器选择一个服务实例
            ServiceInstance instance = loadBalancerClient.choose(serviceName);

            if (instance == null) {
                log.error("No available instances for service: {}", serviceName);
                throw new IOException("No available instances for service: " + serviceName);
            }

            log.info("Selected instance: {}:{} for service: {}",
                    instance.getHost(), instance.getPort(), serviceName);

            // 构建实际的 URL
            String actualUrl = String.format("http://%s:%d", instance.getHost(), instance.getPort());
            
            // 创建新的 RequestTemplate 以避免修改原对象
            RequestTemplate newTemplate = RequestTemplate.builder()
                    .method(requestTemplate.getMethod())
                    .url(actualUrl)
                    .path(requestTemplate.getPath())
                    .headers(requestTemplate.getHeaders() != null ? new HashMap<>(requestTemplate.getHeaders()) : new HashMap<>())
                    .queryParams(requestTemplate.getQueryParams() != null ? new HashMap<>(requestTemplate.getQueryParams()) : new HashMap<>())
                    .pathVariables(requestTemplate.getPathVariables() != null ? new HashMap<>(requestTemplate.getPathVariables()) : new HashMap<>())
                    .body(requestTemplate.getBody())
                    .build();

            log.debug("Transformed URL from '{}' to '{}'" , serviceName, actualUrl);

            // 执行请求
            return delegate.execute(newTemplate);

        } catch (Exception e) {
            log.error("Failed to execute request with load balancer for service: {}", serviceName, e);
            throw new IOException("Load balancer execution failed: " + e.getMessage(), e);
        }}
}
