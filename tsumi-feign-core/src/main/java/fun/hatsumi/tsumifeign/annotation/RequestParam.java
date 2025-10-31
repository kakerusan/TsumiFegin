package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * 请求参数注解
 *
 * @author Kakeru
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    /**
     * 参数名
     */
    String value() default "";

    /**
     * 参数名（别名）
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
