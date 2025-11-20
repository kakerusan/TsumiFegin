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
    /**
     * 创建 Encoder
     * 默认使用 FastJsonEncoder
     * @return Encoder
     */
    @Bean
    @ConditionalOnMissingBean
    public Encoder encoder() {
        log.info("Creating default FastJsonEncoder");
        return new FastJsonEncoder();
    }

    /**
     *  创建 Decoder
     *   默认使用 FastJsonDecoder
     * @return Decoder
     */

    @Bean
    @ConditionalOnMissingBean
    public Decoder decoder() {
        log.info("Creating default FastJsonDecoder");
        return new FastJsonDecoder();
    }

    /**
     *
     * 创建 AnnotationContract
     * AnnotationContract 是 Feign 的核心，用于解析注解
     * @return AnnotationContract
     */

    @Bean
    @ConditionalOnMissingBean
    public AnnotationContract annotationContract() {
        log.info("Creating AnnotationContract");
        return new AnnotationContract();
    }

    /**
     *  创建 OkHttpClient
     *  默认使用 OkHttpClient
     *  配置项：
     *   - connectTimeout: 连接超时时间，默认 10 秒
     *   - readTimeout: 读取超时，默认 10 秒
     *   - writeTimeout: 写入超时，默认 10 秒
     *   - connectionPool: 连接池配置
     *   - maxIdleConnections: 最大空闲连接数，默认 5
     *   - keepAliveDuration: 空闲连接保持时间，默认 5 分钟
     *   - maxRequests: 最大请求数，默认 5
     *   - maxRequestsPerHost: 每个主机的最大请求数，默认 5
     *   - maxRequestsPerRoute: 每个路由的最大请求数，默认 5
     *
     * @param properties 配置项
     * @return OkHttpClient
     */
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

    /**
     *  创建 httpFeignClient
     *  默认使用 OkHttpFeignClient
     * @param okHttpClient OkHttpClient
     * @return FeignClient
     */
    @Bean(name = "httpFeignClient")
    @ConditionalOnMissingBean(name = "httpFeignClient")
    public FeignClient httpFeignClient(OkHttpClient okHttpClient) {
        log.info("Creating httpFeignClient (OkHttpFeignClient)");
        return new OkHttpFeignClient(okHttpClient);
    }

    /**
     * 默认的 FeignClient 实现，指向 httpFeignClient
     * 当没有指定 clientType 时使用
     */
    @Bean
    @ConditionalOnMissingBean(FeignClient.class)
    public FeignClient defaultFeignClient(OkHttpClient okHttpClient) {
        log.info("Creating default FeignClient (delegates to httpFeignClient)");
        return new OkHttpFeignClient(okHttpClient);
    }
}
