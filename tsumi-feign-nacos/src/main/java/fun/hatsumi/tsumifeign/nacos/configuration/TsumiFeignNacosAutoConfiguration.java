package fun.hatsumi.tsumifeign.nacos.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.nacos.client.LoadBalancerFeignClient;
import fun.hatsumi.tsumifeign.nacos.loadbalancer.NacosServiceInstanceListSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * TsumiFeign Nacos 自动配置
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = "fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignAutoConfiguration")
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
