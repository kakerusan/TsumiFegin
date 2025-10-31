package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * 请求体注解
 *
 * @author Kakeru
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {

    /**
     * 是否必需
     */
    boolean required() default true;
}
