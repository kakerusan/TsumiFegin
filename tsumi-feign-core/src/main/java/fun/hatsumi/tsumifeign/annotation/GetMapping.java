package fun.hatsumi.tsumifeign.annotation;

import java.lang.annotation.*;

/**
 * HTTP GET 请求注解
 *
 * @author Kakeru
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GetMapping {

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
