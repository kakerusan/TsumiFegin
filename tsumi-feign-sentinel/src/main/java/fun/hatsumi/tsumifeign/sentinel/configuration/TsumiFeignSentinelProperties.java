package fun.hatsumi.tsumifeign.sentinel.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TsumiFeign Sentinel 配置属性
 *
 * @author kakeru
 */
@Data
@ConfigurationProperties(prefix = "tsumi.feign.sentinel")
public class TsumiFeignSentinelProperties {

    /**
     * 是否启用 Sentinel 集成
     */
    private boolean enabled = true;

    /**
     * 资源名称前缀
     */
    private String resourcePrefix = "tsumi-feign";

    /**
     * 是否启用降级
     */
    private boolean fallbackEnabled = true;

    /**
     * 流控配置
     */
    private FlowControl flowControl = new FlowControl();

    /**
     * 熔断配置
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * 流控配置
     */
    @Data
    public static class FlowControl {
        /**
         * 是否启用流控
         */
        private boolean enabled = true;

        /**
         * QPS 阈值
         */
        private double qpsThreshold = 100.0;

        /**
         * 并发线程数阈值
         */
        private int threadThreshold = 50;
    }

    /**
     * 熔断配置
     */
    @Data
    public static class CircuitBreaker {
        /**
         * 是否启用熔断
         */
        private boolean enabled = true;

        /**
         * 慢调用比例阈值（0.0 - 1.0）
         */
        private double slowRatioThreshold = 0.5;

        /**
         * 慢调用时间阈值（毫秒）
         */
        private long slowCallDuration = 1000;

        /**
         * 异常比例阈值（0.0 - 1.0）
         */
        private double errorRatioThreshold = 0.5;

        /**
         * 异常数阈值
         */
        private int errorCount = 10;

        /**
         * 最小请求数（触发熔断的最小请求数）
         */
        private int minRequestAmount = 5;

        /**
         * 熔断时长（毫秒）
         */
        private long breakerDuration = 10000;
    }
}
