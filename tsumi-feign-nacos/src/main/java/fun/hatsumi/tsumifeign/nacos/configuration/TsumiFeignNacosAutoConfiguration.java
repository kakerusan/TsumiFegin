package fun.hatsumi.tsumifeign.nacos.configuration;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.nacos.client.LoadBalancerFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * TsumiFeign Nacos 自动配置
 * 为所有服务配置使用 Nacos 作为服务发现和负载均衡
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = "fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignAutoConfiguration")
@EnableConfigurationProperties(TsumiFeignNacosProperties.class)
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfiguration.class)
public class TsumiFeignNacosAutoConfiguration {

    @Bean(name = "loadBalancerFeignClient")
    @Primary
    public FeignClient loadBalancerFeignClient(
            @Lazy @Qualifier("httpFeignClient") FeignClient delegate,
            LoadBalancerClient loadBalancerClient) {

        log.info("=== Creating LoadBalancerFeignClient with Nacos integration ===");

        return new LoadBalancerFeignClient(delegate, loadBalancerClient);
    }

    /**
     * 为支持 clientType=\"nacos\" 注册别名
     */
    @Bean(name = "nacosFeignClient")
    public FeignClient nacosFeignClient(
            @Lazy @Qualifier("httpFeignClient") FeignClient delegate,
            LoadBalancerClient loadBalancerClient) {

        log.info("Creating nacosFeignClient (same as loadBalancerFeignClient)");
        return new LoadBalancerFeignClient(delegate, loadBalancerClient);
    }
}
