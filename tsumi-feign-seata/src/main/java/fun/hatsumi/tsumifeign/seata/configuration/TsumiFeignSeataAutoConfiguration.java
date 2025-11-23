package fun.hatsumi.tsumifeign.seata.configuration;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.seata.client.SeataFeignClient;
import fun.hatsumi.tsumifeign.seata.interceptor.SeataTransactionInterceptor;
import io.seata.spring.annotation.GlobalTransactionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TsumiFeign Seata 自动配置
 * 配置Seata事务传播客户端和拦截器，实现分布式事务的透明传播
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@ConditionalOnClass(GlobalTransactionScanner.class)
@ConditionalOnProperty(value = "tsumifeign.seata.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TsumiFeignSeataProperties.class)
@AutoConfigureAfter(name = "fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignAutoConfiguration")
public class TsumiFeignSeataAutoConfiguration {

    /**
     * 注册Seata事务传播客户端（与Sentinel集成）
     * 如果存在SentinelFeignClient，则包装它
     */
    @Bean(name = "seataFeignClient")
    @Primary
    @ConditionalOnMissingBean(name = "seataFeignClient")
    public FeignClient seataFeignClient(
            @Lazy FeignClient delegate,
            TsumiFeignSeataProperties properties) {

        log.info("=== Creating SeataFeignClient with transaction propagation ===");
        log.info("XID Header: {}", properties.getXidHeaderName());
        log.info("Branch Type Header: {}", properties.getBranchTypeHeaderName());
        log.info("Log XID: {}", properties.isLogXid());

        return new SeataFeignClient(delegate, properties);
    }

    /**
     * 注册事务拦截器（用于服务端接收XID）
     */
    @Bean
    public SeataTransactionInterceptor seataTransactionInterceptor(
            TsumiFeignSeataProperties properties) {
        log.info("Registering Seata transaction interceptor");
        return new SeataTransactionInterceptor(properties);
    }

    /**
     * 注册拦截器到Spring MVC
     */
    @Bean
    public WebMvcConfigurer seataWebMvcConfigurer(
            SeataTransactionInterceptor interceptor) {
        log.info("Configuring Seata interceptor for Spring MVC");
        
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**")
                        .order(0); // 确保在其他拦截器之前执行
                log.info("Seata transaction interceptor registered successfully");
            }
        };
    }
}
