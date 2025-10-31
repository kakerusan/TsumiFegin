package fun.hatsumi.tsumifeign.spring.configuration;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.client.OkHttpFeignClient;
import fun.hatsumi.tsumifeign.codec.Decoder;
import fun.hatsumi.tsumifeign.codec.Encoder;
import fun.hatsumi.tsumifeign.codec.FastJsonDecoder;
import fun.hatsumi.tsumifeign.codec.FastJsonEncoder;
import fun.hatsumi.tsumifeign.contract.AnnotationContract;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * TsumiFeign 自动配置
 *
 * @author kakeru
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TsumiFeignProperties.class)
public class TsumiFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Encoder encoder() {
        log.info("Creating default FastJsonEncoder");
        return new FastJsonEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public Decoder decoder() {
        log.info("Creating default FastJsonDecoder");
        return new FastJsonDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnnotationContract annotationContract() {
        log.info("Creating AnnotationContract");
        return new AnnotationContract();
    }

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(TsumiFeignProperties properties) {
        log.info("Creating OkHttpClient with config: {}", properties);
        
        TsumiFeignProperties.OkHttpConfig config = properties.getOkhttp();
        
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(
                        config.getConnectionPool().getMaxIdleConnections(),
                        config.getConnectionPool().getKeepAliveDuration(),
                        TimeUnit.SECONDS
                ))
                .build();
    }

    @Bean(name = "okHttpFeignClient")
    @ConditionalOnMissingBean(FeignClient.class)
    public FeignClient okHttpFeignClient(OkHttpClient okHttpClient) {
        log.info("Creating OkHttpFeignClient");
        return new OkHttpFeignClient(okHttpClient);
    }
}
