package fun.hatsumi.tsumifeign.factory;

import fun.hatsumi.tsumifeign.annotation.TsumiFeignClient;
import fun.hatsumi.tsumifeign.proxy.FeignInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feign 客户端工厂
 * 负责创建和管理 Feign 客户端实例
 *
 * @author hatsumi
 */
@Slf4j
public class TsumiFeignClientFactory {

    /**
     * 客户端实例缓存
     */
    private static final Map<Class<?>, Object> CLIENT_CACHE = new ConcurrentHashMap<>();

    /**
     * 创建 Feign 客户端实例
     *
     * @param interfaceType 接口类型
     * @param <T> 客户端类型
     * @return 客户端实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceType) {
        // 验证接口
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("Type must be an interface");
        }

        if (!interfaceType.isAnnotationPresent(TsumiFeignClient.class)) {
            throw new IllegalArgumentException("Interface must have @TsumiFeignClient annotation");
        }

        // 从缓存中获取
        return (T) CLIENT_CACHE.computeIfAbsent(interfaceType, key -> {
            log.info("Creating Feign client for interface: {}", interfaceType.getName());

            // 创建动态代理
            FeignInvocationHandler handler = new FeignInvocationHandler(interfaceType);

            return Proxy.newProxyInstance(
                    interfaceType.getClassLoader(),
                    new Class<?>[]{interfaceType},
                    handler
            );
        });
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        CLIENT_CACHE.clear();
    }

    /**
     * 移除指定客户端缓存
     */
    public static void removeClient(Class<?> interfaceType) {
        CLIENT_CACHE.remove(interfaceType);
    }
}
