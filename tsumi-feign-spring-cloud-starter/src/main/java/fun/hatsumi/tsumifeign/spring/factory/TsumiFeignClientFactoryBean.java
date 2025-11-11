package fun.hatsumi.tsumifeign.spring.factory;

import fun.hatsumi.tsumifeign.annotation.TsumiFeignClient;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.codec.Decoder;
import fun.hatsumi.tsumifeign.codec.Encoder;
import fun.hatsumi.tsumifeign.contract.AnnotationContract;
import fun.hatsumi.tsumifeign.proxy.FeignInvocationHandler;
import fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

/**
 * TsumiFeign 客户端 FactoryBean
 * 用于在 Spring 容器中创建 Feign 客户端实例
 *
 * @author hatsumi
 */
@Data
@Slf4j
public class TsumiFeignClientFactoryBean implements FactoryBean<Object>,
        InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 客户端接口类型
     */
    private String type;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务 URL
     */
    private String url;

    /**
     * 路径前缀
     */
    private String path;

    /**
     * 客户端实例
     */
    private Object target;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("Initializing TsumiFeign client for type: {}", type);
    }

    @Override
    public Object getObject() throws Exception {
        if (target == null) {
            Class<?> clientType = Class.forName(type);
            log.info("Creating TsumiFeign client instance for: {}", clientType.getName());
            
            // 获取注解中的 clientType
            TsumiFeignClient annotation = clientType.getAnnotation(TsumiFeignClient.class);
            String annotationClientType = annotation != null ? annotation.clientType() : "";
            
            // 确定最终使用的 clientType：注解级 > 全局配置
            String finalClientType = determineFinalClientType(annotationClientType);
            log.debug("Using client type: {} for interface: {}", finalClientType, clientType.getName());
            
            // 从 Spring 容器获取对应的 FeignClient 实现
            FeignClient feignClient = resolveFeignClient(finalClientType);
            Encoder encoder = applicationContext.getBean(Encoder.class);
            Decoder decoder = applicationContext.getBean(Decoder.class);
            AnnotationContract contract = applicationContext.getBean(AnnotationContract.class);
            
            // 创建动态代理
            FeignInvocationHandler handler = new FeignInvocationHandler(
                    clientType, feignClient, encoder, decoder, contract);
            
            target = Proxy.newProxyInstance(
                    clientType.getClassLoader(),
                    new Class<?>[]{clientType},
                    handler
            );
        }
        return target;
    }

    /**
     * 确定最终使用的 clientType
     * 优先级：注解级 > 全局配置
     */
    private String determineFinalClientType(String annotationClientType) {
        // 如果注解中指定了 clientType，优先使用
        if (StringUtils.hasText(annotationClientType)) {
            return annotationClientType;
        }
        
        // 否则使用全局配置
        try {
            TsumiFeignProperties properties = applicationContext.getBean(TsumiFeignProperties.class);
            return properties.getClient().getDefaultClientType();
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("TsumiFeignProperties not found, using default client type: http");
            return "http";
        }
    }

    /**
     * 根据 clientType 从容器获取对应的 FeignClient 实现
     */
    private FeignClient resolveFeignClient(String clientType) {
        String beanName = clientType + "FeignClient";
        
        try {
            // 尝试按名称获取具体的 FeignClient 实现
            FeignClient client = applicationContext.getBean(beanName, FeignClient.class);
            log.debug("Found FeignClient bean: {}", beanName);
            return client;
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("FeignClient bean '{}' not found, trying to get default FeignClient", beanName);
            
            // 如果找不到具体的，尝试获取默认的
            try {
                return applicationContext.getBean(FeignClient.class);
            } catch (NoSuchBeanDefinitionException ex) {
                throw new IllegalStateException(
                    String.format("No FeignClient implementation found for type '%s'. " +
                        "Please ensure either '%s' bean or a default FeignClient bean is registered.",
                        clientType, beanName), ex);
            }
        }
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return type != null ? Class.forName(type) : null;
        } catch (ClassNotFoundException e) {
            log.error("Failed to load client type: {}", type, e);
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
