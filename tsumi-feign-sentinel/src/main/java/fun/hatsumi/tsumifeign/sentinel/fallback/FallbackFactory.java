package fun.hatsumi.tsumifeign.sentinel.fallback;

import fun.hatsumi.tsumifeign.core.Response;

/**
 * Fallback 工厂接口
 * 用于创建降级响应
 *
 * @author kakeru
 */
@FunctionalInterface
public interface FallbackFactory<T> {

    /**
     * 创建降级响应
     *
     * @param cause 异常原因
     * @return 降级响应
     */
    Response create(Throwable cause);
}
