package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * TsumiFeign 客户端注解
 * 用于标识一个接口为 Feign 客户端
 *
 * @author Kakeru
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TsumiFeignClient {

    /**
     * 服务名称，用于服务发现
     */
    String name() default "";

    /**
     * 服务 URL，如果指定则直接使用该 URL，不进行服务发现
     */
    String url() default "";

    /**
     * 请求路径前缀
     */
    String path() default "";

    /**
     * 降级处理类
     */
    Class<?> fallback() default void.class;

    /**
     * 降级工厂类
     */
    Class<?> fallbackFactory() default void.class;
}
