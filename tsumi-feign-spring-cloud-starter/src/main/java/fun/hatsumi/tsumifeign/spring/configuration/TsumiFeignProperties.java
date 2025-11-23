package fun.hatsumi.tsumifeign.spring.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TsumiFeign 配置属性
 *
 * @author kakeru
 */
@Data
@ConfigurationProperties(prefix = "tsumi.feign")
public class TsumiFeignProperties {

    /**
     * OkHttp 配置
     */
    private OkHttpConfig okhttp = new OkHttpConfig();

    /**
     * 客户端配置
     */
    private ClientConfig client = new ClientConfig();

    /**
     * LoadBalancer 配置
     */
    private LoadBalancerConfig loadbalancer = new LoadBalancerConfig();

    @Data
    public static class OkHttpConfig {
        /**
         * 连接超时（毫秒）
         */
        private long connectTimeout = 5000;

        /**
         * 读超时（毫秒）
         */
        private long readTimeout = 10000;

        /**
         * 写超时（毫秒）
         */
        private long writeTimeout = 10000;

        /**
         * 连接池配置
         */
        private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();
    }

    @Data
    public static class ConnectionPoolConfig {
        /**
         * 最大空闲连接数
         */
        private int maxIdleConnections = 200;

        /**
         * 连接保持时间（秒）
         */
        private long keepAliveDuration = 300;
    }

    @Data
    public static class ClientConfig {
        /**
         * 客户端类型：http, triple, custom
         * 默认为 http
         */
        private String defaultClientType = "http";

        /**
         * 日志级别：NONE, BASIC, HEADERS, FULL
         */
        private String logLevel = "BASIC";

        /**
         * 是否启用重试
         */
        private boolean retryEnabled = true;

        /**
         * 最大重试次数
         */
        private int maxRetries = 3;
    }

    @Data
    public static class LoadBalancerConfig {
        /**
         * 负载均衡策略：round-robin, random, weighted
         */
        private String strategy = "round-robin";

        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
}
