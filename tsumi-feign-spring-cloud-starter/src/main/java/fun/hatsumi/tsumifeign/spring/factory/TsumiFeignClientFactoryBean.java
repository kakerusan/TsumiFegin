package fun.hatsumi.tsumifeign.spring.factory;

import fun.hatsumi.tsumifeign.annotation.TsumiFeignClient;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.codec.Decoder;
import fun.hatsumi.tsumifeign.codec.Encoder;
import fun.hatsumi.tsumifeign.contract.AnnotationContract;
import fun.hatsumi.tsumifeign.proxy.FeignInvocationHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
            
            // 从 Spring 容器获取依赖
            FeignClient feignClient = applicationContext.getBean(FeignClient.class);
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
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
