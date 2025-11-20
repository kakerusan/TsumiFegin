package fun.hatsumi.tsumifeign.spring.configuration;

import fun.hatsumi.tsumifeign.annotation.TsumiFeignClient;
import fun.hatsumi.tsumifeign.spring.annotation.EnableTsumiFeignClients;
import fun.hatsumi.tsumifeign.spring.factory.TsumiFeignClientFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TsumiFeign 客户端注册器
 * 扫描并注册带有 @TsumiFeignClient 注解的接口
 *
 * @author kakeru
 */
/*
ImportBeanDefinitionRegistrar
 */
@Slf4j
public class TsumiFeignClientsRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        @NotNull BeanDefinitionRegistry registry) {
        // 获取 @EnableTsumiFeignClients 注解的属性
        Map<String, Object> attrs = metadata.getAnnotationAttributes(
                EnableTsumiFeignClients.class.getName());

        if (attrs == null) {
            return;
        }

        // 获取要扫描的包路径
        Set<String> basePackages = getBasePackages(metadata, attrs);

        log.info("Scanning for TsumiFeign clients in packages: {}", basePackages);

        // 扫描并注册客户端
        registerClients(basePackages, registry);
    }

    /**
     * 获取要扫描的基础包路径
     */
    private Set<String> getBasePackages(AnnotationMetadata metadata, Map<String, Object> attrs) {
        Set<String> basePackages = new HashSet<>();

        // 从 value 属性获取
        String[] value = (String[]) attrs.get("value");
        if (value != null) {
            basePackages.addAll(Arrays.asList(value));
        }

        // 从 basePackages 属性获取
        String[] packages = (String[]) attrs.get("basePackages");
        if (packages != null) {
            basePackages.addAll(Arrays.asList(packages));
        }

        // 从 basePackageClasses 属性获取
        Class<?>[] basePackageClasses = (Class<?>[]) attrs.get("basePackageClasses");
        if (basePackageClasses != null) {
            for (Class<?> clazz : basePackageClasses) {
                basePackages.add(ClassUtils.getPackageName(clazz));
            }
        }

        // 如果没有指定，使用当前配置类所在的包
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }

        return basePackages;
    }

    /**
     * 扫描并注册客户端
     */
    private void registerClients(Set<String> basePackages, BeanDefinitionRegistry registry) {
        // 创建扫描器 Spring提供的
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        // 添加过滤器，只扫描带有 @TsumiFeignClient 注解的所有人 不单单是接口
        scanner.addIncludeFilter(new AnnotationTypeFilter(TsumiFeignClient.class));

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition beanDefinition) {
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();

                    // 验证是否为接口
                    if (!annotationMetadata.isInterface()) {
                        log.warn("@TsumiFeignClient can only be specified on an interface: {}",
                                annotationMetadata.getClassName());
                        continue;
                    }

                    // 注册客户端
                    registerClientBean(beanDefinition, annotationMetadata, registry);
                }
            }
        }
    }

    /**
     * 注册客户端 Bean
     */
    private void registerClientBean(AnnotatedBeanDefinition beanDefinition,
                                    AnnotationMetadata annotationMetadata,
                                    BeanDefinitionRegistry registry) {
        // 获取属性
        String className = annotationMetadata.getClassName();
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(
                TsumiFeignClient.class.getName());

        if (attributes == null) {
            return;
        }

        String name = getClientName(attributes);
        String beanName = name + "TsumiFeignClient";

        log.info("Registering TsumiFeign client: {} for interface: {}", beanName, className);

        // 创建 FactoryBean 定义   注入代理对象TsumiFeignClientFactoryBean 到 IOC 容器
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(TsumiFeignClientFactoryBean.class);

        builder.addPropertyValue("type", className);
        builder.addPropertyValue("name", name);
        builder.addPropertyValue("url", getUrl(attributes));
        builder.addPropertyValue("path", getPath(attributes));
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        AbstractBeanDefinition definition = builder.getBeanDefinition();
        definition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 获取客户端名称
     */
    private String getClientName(Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            name = (String) attributes.get("value");
        }
        return StringUtils.hasText(name) ? name : "";
    }

    /**
     * 获取 URL
     */
    private String getUrl(Map<String, Object> attributes) {
        String url = (String) attributes.get("url");
        return StringUtils.hasText(url) ? url : "";
    }

    /**
     * 获取路径
     */
    private String getPath(Map<String, Object> attributes) {
        String path = (String) attributes.get("path");
        return StringUtils.hasText(path) ? path : "";
    }

    /**
     * 创建扫描器
     */
    private ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
                // 只接受顶层接口
                return beanDefinition.getMetadata().isIndependent()
                        && !beanDefinition.getMetadata().isAnnotation();
            }
        };
    }
}
