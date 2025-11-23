package fun.hatsumi.tsumifeign.client;

import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 客户端实现
 *
 * @author Kakeru
 */
@Slf4j
public class OkHttpFeignClient implements FeignClient {

    private final OkHttpClient okHttpClient;

    public OkHttpFeignClient() {
        this(new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(10, TimeUnit.SECONDS) // 读取超时
                .writeTimeout(10, TimeUnit.SECONDS) // 写超时
                .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES)) // 连接池
                .build());
    }

    public OkHttpFeignClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public Response execute(RequestTemplate requestTemplate) throws IOException {
        // 构建 OkHttp Request
        Request request = buildRequest(requestTemplate);

        log.debug("Executing request: {} {}", requestTemplate.getMethod(), request.url());

        // 执行请求
        try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            return buildResponse(response);
        }
    }

    /**
     * 构建 OkHttp Request
     */
    private Request buildRequest(RequestTemplate requestTemplate) {
        Request.Builder builder = new Request.Builder();

        // 设置 URL
        String url = requestTemplate.buildUrl();
        builder.url(url);

        // 设置请求头
        if (requestTemplate.getHeaders() != null) {
            requestTemplate.getHeaders().forEach(builder::addHeader);
        }

        // 设置请求体和方法
        RequestBody requestBody = buildRequestBody(requestTemplate);
        builder.method(requestTemplate.getMethod(), requestBody);

        return builder.build();
    }

    /**
     * 构建请求体
     */
    private RequestBody buildRequestBody(RequestTemplate requestTemplate) {
        String method = requestTemplate.getMethod();

        // GET 和 DELETE 请求不需要请求体
        if ("GET".equals(method) || "DELETE".equals(method)) {
            return null;
        }

        Object body = requestTemplate.getBody();
        if (body == null) {
            return RequestBody.create(new byte[0]);
        }

        if (body instanceof byte[]) {
            String contentType = requestTemplate.getHeaders().getOrDefault
                    ("Content-Type", "application/json; charset=UTF-8");
            return RequestBody.create((byte[]) body, MediaType.parse(contentType));
        }

        return RequestBody.create(new byte[0]);
    }

    /**
     * 构建响应
     */
    private Response buildResponse(okhttp3.Response response) throws IOException {
        Response feignResponse = new Response();
        feignResponse.setStatus(response.code());

        // 设置响应头
        Map<String, String> headers = new HashMap<>();
        response.headers()
                .forEach(pair
                        -> headers.put(pair.getFirst(), pair.getSecond()));
        feignResponse.setHeaders(headers);

        // 设置响应体
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            feignResponse.setBody(responseBody.bytes());
        }

        return feignResponse;
    }
}
