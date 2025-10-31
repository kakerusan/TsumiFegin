package fun.hatsumi.tsumifeign.sentinel.configuration;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.sentinel.client.SentinelFeignClient;
import fun.hatsumi.tsumifeign.sentinel.fallback.DefaultFallbackFactory;
import fun.hatsumi.tsumifeign.sentinel.fallback.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TsumiFeign Sentinel 自动配置
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@ConditionalOnClass(SentinelResourceAspect.class)
@ConditionalOnProperty(value = "tsumi.feign.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(TsumiFeignSentinelProperties.class)
public class TsumiFeignSentinelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        log.info("Initializing Sentinel resource aspect");
        return new SentinelResourceAspect();
    }

    @Bean
    @ConditionalOnMissingBean(FallbackFactory.class)
    public FallbackFactory<?> defaultFallbackFactory() {
        log.info("Creating default FallbackFactory");
        return new DefaultFallbackFactory();
    }

    @Bean
    @ConditionalOnProperty(value = "tsumi.feign.sentinel.enabled", matchIfMissing = true)
    public FeignClient sentinelFeignClient(
            FeignClient delegate,
            FallbackFactory<?> fallbackFactory,
            TsumiFeignSentinelProperties properties) {

        log.info("Creating SentinelFeignClient with resource prefix: {}", 
                properties.getResourcePrefix());

        return new SentinelFeignClient(
                delegate, 
                fallbackFactory,
                properties.getResourcePrefix()
        );
    }
}
