package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * 请求头注解
 *
 * @author Kakeru
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeader {

    /**
     * 请求头名称
     */
    String value() default "";

    /**
     * 请求头名称（别名）
     */
    String name() default "";

    /**
     * 是否必需
     */
    boolean required() default true;

    /**
     * 默认值
     */
    String defaultValue() default "";
}
