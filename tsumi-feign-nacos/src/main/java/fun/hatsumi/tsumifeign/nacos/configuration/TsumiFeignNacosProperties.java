package fun.hatsumi.tsumifeign.nacos.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TsumiFeign Nacos 配置属性
 *
 * @author kakeru
 */
@Data
@ConfigurationProperties(prefix = "tsumi.feign.nacos")
public class TsumiFeignNacosProperties {

    /**
     * 是否启用 Nacos 集成
     */
    private boolean enabled = true;

    /**
     * 服务发现配置
     */
    private DiscoveryConfig discovery = new DiscoveryConfig();

    /**
     * 负载均衡配置
     */
    private LoadBalancerConfig loadBalancer = new LoadBalancerConfig();

    @Data
    public static class DiscoveryConfig {
        /**
         * 是否只获取健康实例
         */
        private boolean healthyOnly = true;

        /**
         * 订阅服务变更
         */
        private boolean subscribe = true;

        /**
         * 实例刷新间隔（毫秒）
         */
        private long refreshInterval = 30000;
    }

    @Data
    public static class LoadBalancerConfig {
        /**
         * 负载均衡策略
         */
        private String strategy = "round-robin";

        /**
         * 是否启用权重
         */
        private boolean weightEnabled = true;

        /**
         * 是否启用同集群优先
         */
        private boolean sameClusterPriority = true;
    }
}
