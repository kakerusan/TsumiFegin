package fun.hatsumi.tsumifeign.nacos.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nacos 服务实例列表提供器
 * 从 Nacos 注册中心获取服务实例列表
 *
 * @author kakeru
 */
@Slf4j
public class NacosServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final String serviceId;
    private final NacosDiscoveryProperties discoveryProperties;
    private final NacosServiceManager nacosServiceManager;

    public NacosServiceInstanceListSupplier(String serviceId,
                                            NacosDiscoveryProperties discoveryProperties,
                                            NacosServiceManager nacosServiceManager) {
        this.serviceId = serviceId;
        this.discoveryProperties = discoveryProperties;
        this.nacosServiceManager = nacosServiceManager;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.defer(() -> {
            try {
                List<ServiceInstance> instances = getInstances();
                return Flux.just(instances);
            } catch (Exception e) {
                log.error("Failed to get instances for service: {}", serviceId, e);
                return Flux.just(Collections.emptyList());
            }
        });
    }

    /**
     * 从 Nacos 获取服务实例
     */
    private List<ServiceInstance> getInstances() throws NacosException {
        NamingService namingService = nacosServiceManager.getNamingService(
                discoveryProperties.getNacosProperties());

        String group = discoveryProperties.getGroup();

        log.debug("Getting instances for service: {}, group: {}",
                serviceId, group);

        // 获取健康的服务实例（只传递 serviceName, groupName, healthy 三个参数）
        List<Instance> instances = namingService.selectInstances(
                serviceId, group, true);

        log.info("Found {} healthy instances for service: {}", instances.size(), serviceId);

        return convertToServiceInstances(instances);
    }

    /**
     * 将 Nacos Instance 转换为 Spring Cloud ServiceInstance
     */
    private List<ServiceInstance> convertToServiceInstances(List<Instance> nacosInstances) {
        if (nacosInstances == null || nacosInstances.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServiceInstance> serviceInstances = new ArrayList<>(nacosInstances.size());

        for (Instance instance : nacosInstances) {
            ServiceInstance serviceInstance = new DefaultServiceInstance(
                    instance.getInstanceId(),
                    serviceId,
                    instance.getIp(),
                    instance.getPort(),
<<<<<<< HEAD
                    false, // 是否为 HTTPS，分析 metadata 或设置为 false
=======
                    instance.isHealthy() && instance.isEnabled(),
>>>>>>> 48aee53d536c259b1b5ea51bb87824d7e602542b
                    instance.getMetadata()
            );
            serviceInstances.add(serviceInstance);
        }

        return serviceInstances;
    }
}
