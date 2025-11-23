package fun.hatsumi.tsumifeign.spring.annotation;

import fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 TsumiFeign 客户端扫描
 * 
 * @author kakeru
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TsumiFeignClientsRegistrar.class)
public @interface EnableTsumiFeignClients {

    /**
     * 扫描的基础包路径
     * 如果为空，则扫描当前类所在的包
     */
    String[] value() default {};

    /**
     * 扫描的基础包路径（别名）
     */
    String[] basePackages() default {};

    /**
     * 扫描的基础类
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 默认配置类
     */
    Class<?>[] defaultConfiguration() default {};
}
