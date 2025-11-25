package fun.hatsumi.tsumifeign.nacos.configuration;

<<<<<<< HEAD
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.nacos.client.LoadBalancerFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
=======
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
>>>>>>> 48aee53d536c259b1b5ea51bb87824d7e602542b
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
<<<<<<< HEAD

/**
 * TsumiFeign Nacos 自动配置
 * 为所有服务配置使用 Nacos 作为服务发现和负载均衡
=======
import org.springframework.core.env.Environment;

/**
 * TsumiFeign Nacos 自动配置
>>>>>>> 48aee53d536c259b1b5ea51bb87824d7e602542b
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = "fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignAutoConfiguration")
<<<<<<< HEAD
@EnableConfigurationProperties(TsumiFeignNacosProperties.class)
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfiguration.class)
=======
>>>>>>> 48aee53d536c259b1b5ea51bb87824d7e602542b
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
