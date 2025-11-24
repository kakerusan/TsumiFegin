package fun.hatsumi.tsumifeign.nacos.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import fun.hatsumi.tsumifeign.nacos.configuration.TsumiFeignNacosProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Nacos 权重负载均衡器
 * 支持：
 * 1. 同集群优先策略
 * 2. 基于权重的随机选择
 * 3. 轮询（当权重未启用时）
 *
 * @author kakeru
 */
@Slf4j
public class NacosWeightedLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final TsumiFeignNacosProperties properties;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final AtomicInteger position = new AtomicInteger(new Random().nextInt(1000));

    public NacosWeightedLoadBalancer(
            String serviceId,
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            TsumiFeignNacosProperties properties,
            NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.properties = properties;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(() -> {
                    log.warn("No ServiceInstanceListSupplier available for service: {}", serviceId);
                    return null;
                });

        if (supplier == null) {
            return Mono.just(new EmptyResponse());
        }

        return supplier.get(request).next()
                .map(instances -> processInstanceResponse(instances));
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        log.debug("Available instances for service {}: {}", serviceId, instances.size());

        // 1. 同集群优先过滤
        List<ServiceInstance> filteredInstances = instances;
        if (properties.getLoadBalancer().isSameClusterPriority()) {
            filteredInstances = filterSameClusterInstances(instances);
            if (filteredInstances.isEmpty()) {
                log.debug("No same-cluster instances found, using all instances");
                filteredInstances = instances;
            } else {
                log.debug("Filtered to same-cluster instances: {}", filteredInstances.size());
            }
        }

        // 2. 根据是否启用权重选择实例
        ServiceInstance selectedInstance;
        if (properties.getLoadBalancer().isWeightEnabled()) {
            selectedInstance = chooseByWeight(filteredInstances);
        } else {
            selectedInstance = chooseByRoundRobin(filteredInstances);
        }

        if (selectedInstance == null) {
            return new EmptyResponse();
        }

        log.info("Selected instance for service {}: {}:{}", 
                serviceId, selectedInstance.getHost(), selectedInstance.getPort());

        return new DefaultResponse(selectedInstance);
    }

    /**
     * 过滤同集群实例
     */
    private List<ServiceInstance> filterSameClusterInstances(List<ServiceInstance> instances) {
        String currentCluster = nacosDiscoveryProperties.getClusterName();
        if (currentCluster == null || currentCluster.isEmpty()) {
            return instances;
        }

        return instances.stream()
                .filter(instance -> {
                    Map<String, String> metadata = instance.getMetadata();
                    String instanceCluster = metadata != null ? metadata.get("nacos.cluster") : null;
                    return currentCluster.equals(instanceCluster);
                })
                .collect(Collectors.toList());
    }

    /**
     * 基于权重选择实例
     */
    private ServiceInstance chooseByWeight(List<ServiceInstance> instances) {
        // 计算总权重
        double totalWeight = 0;
        for (ServiceInstance instance : instances) {
            double weight = getWeight(instance);
            if (weight > 0) {
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0) {
            log.debug("Total weight is 0, falling back to round-robin");
            return chooseByRoundRobin(instances);
        }

        // 随机选择
        double random = Math.random() * totalWeight;
        double currentWeight = 0;

        for (ServiceInstance instance : instances) {
            double weight = getWeight(instance);
            if (weight <= 0) {
                continue;
            }

            currentWeight += weight;
            if (random <= currentWeight) {
                return instance;
            }
        }

        // 兜底：返回第一个实例
        return instances.get(0);
    }

    /**
     * 轮询选择实例
     */
    private ServiceInstance chooseByRoundRobin(List<ServiceInstance> instances) {
        int pos = Math.abs(position.incrementAndGet());
        return instances.get(pos % instances.size());
    }

    /**
     * 从实例元数据中获取权重
     */
    private double getWeight(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        if (metadata == null) {
            return 1.0;
        }

        String weightStr = metadata.get("nacos.weight");
        if (weightStr == null || weightStr.isEmpty()) {
            return 1.0;
        }

        try {
            return Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid weight value for instance {}: {}", instance.getInstanceId(), weightStr);
            return 1.0;
        }
    }
}
