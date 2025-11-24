package fun.hatsumi.tsumifeign.nacos.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


/**
 * 负载均衡集成测试
 * 验证 Nacos 服务实例列表提供器和负载均衡功能
 *
 * @author kakeru
 */
@ExtendWith(MockitoExtension.class)
class LoadBalancerIntegrationTest {

    @Mock
    private NacosDiscoveryProperties discoveryProperties;

    @Mock
    private NacosServiceManager nacosServiceManager;

    @Mock
    private NamingService namingService;

    private NacosServiceInstanceListSupplier supplier;

    private static final String SERVICE_ID = "test-service";
    private static final String GROUP = "DEFAULT_GROUP";

    @BeforeEach
    void setUp() throws Exception {
        // 配置 mock
        lenient().when(discoveryProperties.getGroup()).thenReturn(GROUP);
        lenient().when(discoveryProperties.getNacosProperties()).thenReturn(null);
        lenient().when(nacosServiceManager.getNamingService(any())).thenReturn(namingService);

        supplier = new NacosServiceInstanceListSupplier(
                SERVICE_ID,
                discoveryProperties,
                nacosServiceManager
        );
    }

    @Test
    void testGetServiceId() {
        assertEquals(SERVICE_ID, supplier.getServiceId());
    }

    @Test
    void testGetInstancesFromNacos() throws Exception {
        // 准备测试数据 - 多个实例用于验证负载均衡
        Instance instance1 = createNacosInstance("192.168.1.1", 8080, true);
        Instance instance2 = createNacosInstance("192.168.1.2", 8080, true);
        Instance instance3 = createNacosInstance("192.168.1.3", 8080, true);

        when(namingService.selectInstances(eq(SERVICE_ID), eq(GROUP), eq(true)))
                .thenReturn(Arrays.asList(instance1, instance2, instance3));

        // 执行获取实例
        List<ServiceInstance> instances = supplier.get().blockFirst();

        // 验证结果
        assertNotNull(instances);
        assertEquals(3, instances.size(), "应该返回 3 个健康实例");

        // 验证第一个实例
        ServiceInstance serviceInstance1 = instances.get(0);
        assertEquals(SERVICE_ID, serviceInstance1.getServiceId());
        assertEquals("192.168.1.1", serviceInstance1.getHost());
        assertEquals(8080, serviceInstance1.getPort());
        assertFalse(serviceInstance1.isSecure());

        // 验证第二个实例
        ServiceInstance serviceInstance2 = instances.get(1);
        assertEquals("192.168.1.2", serviceInstance2.getHost());

        // 验证第三个实例
        ServiceInstance serviceInstance3 = instances.get(2);
        assertEquals("192.168.1.3", serviceInstance3.getHost());

        // 验证 Nacos API 调用
        verify(namingService).selectInstances(SERVICE_ID, GROUP, true);
    }

    @Test
    void testGetInstancesWithUnhealthyInstance() throws Exception {
        // 准备测试数据 - 包含不健康的实例
        Instance healthyInstance = createNacosInstance("192.168.1.1", 8080, true);
        Instance unhealthyInstance = createNacosInstance("192.168.1.2", 8080, false);

        when(namingService.selectInstances(eq(SERVICE_ID), eq(GROUP), eq(true)))
                .thenReturn(Arrays.asList(healthyInstance));

        // 执行获取实例
        List<ServiceInstance> instances = supplier.get().blockFirst();

        // 验证结果 - 只返回健康实例
        assertNotNull(instances);
        assertEquals(1, instances.size(), "应该只返回 1 个健康实例");
        assertEquals("192.168.1.1", instances.get(0).getHost());
    }

    @Test
    void testGetInstancesWhenNoInstancesAvailable() throws Exception {
        // Nacos 返回空列表
        when(namingService.selectInstances(eq(SERVICE_ID), eq(GROUP), eq(true)))
                .thenReturn(Arrays.asList());

        // 执行获取实例
        List<ServiceInstance> instances = supplier.get().blockFirst();

        // 验证结果
        assertNotNull(instances);
        assertTrue(instances.isEmpty(), "没有可用实例时应返回空列表");
    }

    @Test
    void testGetInstancesWithMetadata() throws Exception {
        // 准备带元数据的实例
        Instance instance = createNacosInstance("192.168.1.1", 8080, true);
        instance.getMetadata().put("version", "1.0.0");
        instance.getMetadata().put("zone", "zone-a");

        when(namingService.selectInstances(eq(SERVICE_ID), eq(GROUP), eq(true)))
                .thenReturn(Arrays.asList(instance));

        // 执行获取实例
        List<ServiceInstance> instances = supplier.get().blockFirst();

        // 验证元数据
        assertNotNull(instances);
        assertEquals(1, instances.size());
        ServiceInstance serviceInstance = instances.get(0);
        assertEquals("1.0.0", serviceInstance.getMetadata().get("version"));
        assertEquals("zone-a", serviceInstance.getMetadata().get("zone"));
    }

    @Test
    void testGetInstancesHandlesException() throws Exception {
        // 模拟 Nacos 异常
        when(namingService.selectInstances(eq(SERVICE_ID), eq(GROUP), eq(true)))
                .thenThrow(new RuntimeException("Nacos connection failed"));

        // 执行获取实例
        List<ServiceInstance> instances = supplier.get().blockFirst();

        // 验证异常处理 - 应返回空列表而不是抛出异常
        assertNotNull(instances);
        assertTrue(instances.isEmpty(), "发生异常时应返回空列表");
    }

    /**
     * 创建 Nacos 实例
     */
    private Instance createNacosInstance(String ip, int port, boolean healthy) {
        Instance instance = new Instance();
        instance.setInstanceId(ip + ":" + port);
        instance.setIp(ip);
        instance.setPort(port);
        instance.setHealthy(healthy);
        instance.setEnabled(true);
        instance.setServiceName(SERVICE_ID);
        return instance;
    }
}
