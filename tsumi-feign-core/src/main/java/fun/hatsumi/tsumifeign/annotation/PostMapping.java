package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * HTTP POST 请求注解
 *
 * @author Kakeru
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostMapping {

    /**
     * 请求路径
     */
    String value() default "";

    /**
     * 请求路径（别名）
     */
    String path() default "";

    /**
     * 请求头
     */
    String[] headers() default {};
}
