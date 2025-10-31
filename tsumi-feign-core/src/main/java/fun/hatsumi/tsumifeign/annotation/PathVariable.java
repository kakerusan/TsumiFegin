package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * 路径变量注解
 *
 * @author Kakeru
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {

    /**
     * 变量名
     */
    String value() default "";

    /**
     * 变量名（别名）
     */
    String name() default "";

    /**
     * 是否必需
     */
    boolean required() default true;
}
