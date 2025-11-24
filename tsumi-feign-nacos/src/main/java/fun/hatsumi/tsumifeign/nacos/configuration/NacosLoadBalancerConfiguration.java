package fun.hatsumi.tsumifeign.nacos.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import fun.hatsumi.tsumifeign.nacos.loadbalancer.NacosServiceInstanceListSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Nacos 负载均衡器配置
 * 为每个服务配置自定义的 ServiceInstanceListSupplier 和 LoadBalancer
 *
 * @author kakeru
 */
@Slf4j
public class NacosLoadBalancerConfiguration {

    /**
     * 配置 Nacos 服务实例列表提供器
     * 从 Nacos 注册中心获取服务实例列表
     */
    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier(
            ConfigurableApplicationContext context,
            NacosDiscoveryProperties discoveryProperties,
            NacosServiceManager nacosServiceManager) {

        Environment environment = context.getEnvironment();
        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        log.info("Creating NacosServiceInstanceListSupplier for service: {}", serviceId);

        return new NacosServiceInstanceListSupplier(
                serviceId,
                discoveryProperties,
                nacosServiceManager
        );
    }

    /**
     * 配置负载均衡器 - 使用轮询策略
     * 可以替换为其他策略，如 RandomLoadBalancer
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        log.info("Creating RoundRobinLoadBalancer for service: {}", serviceId);

        return new RoundRobinLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId
        );
    }
}
