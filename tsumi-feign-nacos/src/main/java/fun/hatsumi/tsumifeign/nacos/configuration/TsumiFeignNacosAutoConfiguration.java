package fun.hatsumi.tsumifeign.nacos.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.nacos.client.LoadBalancerFeignClient;
import fun.hatsumi.tsumifeign.nacos.loadbalancer.NacosServiceInstanceListSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * TsumiFeign Nacos 自动配置
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@ConditionalOnClass({NacosDiscoveryProperties.class, NacosServiceManager.class})
@ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", matchIfMissing = true)
@AutoConfigureAfter(LoadBalancerClientConfiguration.class)
public class TsumiFeignNacosAutoConfiguration {

    @Bean
    @ConditionalOnBean({NacosDiscoveryProperties.class, NacosServiceManager.class})
    public ServiceInstanceListSupplier nacosServiceInstanceListSupplier(
            ConfigurableApplicationContext context,
            NacosDiscoveryProperties discoveryProperties,
            NacosServiceManager nacosServiceManager) {

        Environment environment = context.getEnvironment();
        String serviceId = environment.getProperty("spring.application.name", "unknown");

        log.info("Creating Nacos ServiceInstanceListSupplier for service: {}", serviceId);

        return new NacosServiceInstanceListSupplier(
                serviceId, discoveryProperties, nacosServiceManager);
    }

    @Bean
    @ConditionalOnBean(LoadBalancerClient.class)
    public FeignClient loadBalancerFeignClient(
            FeignClient delegate,
            LoadBalancerClient loadBalancerClient) {

        log.info("Creating LoadBalancerFeignClient with Nacos integration");

        return new LoadBalancerFeignClient(delegate, loadBalancerClient);
    }
}
