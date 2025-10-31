package fun.hatsumi.tsumifeign.proxy;

import fun.hatsumi.tsumifeign.annotation.TsumiFeignClient;
import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.client.OkHttpFeignClient;
import fun.hatsumi.tsumifeign.codec.Decoder;
import fun.hatsumi.tsumifeign.codec.Encoder;
import fun.hatsumi.tsumifeign.codec.FastJsonDecoder;
import fun.hatsumi.tsumifeign.codec.FastJsonEncoder;
import fun.hatsumi.tsumifeign.contract.AnnotationContract;
import fun.hatsumi.tsumifeign.core.MethodMetadata;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feign 调用处理器
 * 使用 JDK 动态代理实现方法拦截
 *
 * @author hatsumi
 */
@Slf4j
public class FeignInvocationHandler implements InvocationHandler {

    private final Class<?> targetType;
    private final String baseUrl;
    private final FeignClient feignClient;
    private final Encoder encoder;
    private final Decoder decoder;
    private final AnnotationContract contract;

    /**
     * 方法元数据缓存
     */
    private final Map<Method, MethodMetadata> metadataCache = new ConcurrentHashMap<>();

    public FeignInvocationHandler(Class<?> targetType) {
        this.targetType = targetType;
        this.feignClient = new OkHttpFeignClient();
        this.encoder = new FastJsonEncoder();
        this.decoder = new FastJsonDecoder();
        this.contract = new AnnotationContract();

        // 解析类注解获取 baseUrl
        TsumiFeignClient annotation = targetType.getAnnotation(TsumiFeignClient.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Interface must have @TsumiFeignClient annotation");
        }
        this.baseUrl = annotation.url();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 处理 Object 类的方法
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        // 获取或解析方法元数据
        MethodMetadata metadata = metadataCache.computeIfAbsent(method, contract::parseMethod);

        // 构建请求模板
        RequestTemplate requestTemplate = buildRequestTemplate(metadata, args);

        // 执行请求
        Response response = feignClient.execute(requestTemplate);

        // 处理响应
        return handleResponse(response, metadata);
    }

    /**
     * 构建请求模板
     */
    private RequestTemplate buildRequestTemplate(MethodMetadata metadata, Object[] args) {
        RequestTemplate template = RequestTemplate.builder()
                .method(metadata.getHttpMethod())
                .url(baseUrl)
                .path(metadata.getPath())
                .build();

        // 设置请求头
        if (metadata.getHeaders() != null) {
            for (String header : metadata.getHeaders()) {
                String[] parts = header.split(":");
                if (parts.length == 2) {
                    template.addHeader(parts[0].trim(), parts[1].trim());
                }
            }
        }

        // 处理参数
        if (metadata.getParameters() != null && args != null) {
            for (MethodMetadata.ParameterMetadata param : metadata.getParameters()) {
                Object value = args[param.getIndex()];
                if (value == null) {
                    continue;
                }

                switch (param.getParamType()) {
                    case PATH:
                        template.addPathVariable(param.getName(), value);
                        break;
                    case QUERY:
                        template.addQueryParam(param.getName(), value);
                        break;
                    case HEADER:
                        template.addHeader(param.getName(), String.valueOf(value));
                        break;
                    case BODY:
                        // 使用编码器编码请求体
                        byte[] encodedBody = encoder.encode(value);
                        template.setBody(encodedBody);
                        template.addHeader("Content-Type", encoder.getContentType());
                        break;
                }
            }
        }

        return template;
    }

    /**
     * 处理响应
     */
    private Object handleResponse(Response response, MethodMetadata metadata) {
        // 检查响应状态
        if (!response.isSuccess()) {
            log.error("Request failed with status: {}", response.getStatus());
            throw new RuntimeException("Request failed with status: " + response.getStatus());
        }

        // 如果返回类型是 void，直接返回 null
        if (metadata.getReturnType() == void.class || metadata.getReturnType() == Void.class) {
            return null;
        }

        // 如果返回类型是 Response，直接返回响应对象
        if (metadata.getReturnType() == Response.class) {
            return response;
        }

        // 使用解码器解码响应体
        return decoder.decode(response.getBody(), metadata.getReturnType());
    }
}
