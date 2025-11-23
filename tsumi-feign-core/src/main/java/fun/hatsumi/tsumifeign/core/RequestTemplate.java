package fun.hatsumi.tsumifeign.core;

import lombok.Data;
import lombok.Builder;

import java.util.Map;
import java.util.HashMap;

/**
 * HTTP 请求模板
 * 封装了 HTTP 请求的所有信息
 *
 * @author Kakeru
 */
@Data
@Builder
public class RequestTemplate {

    /**
     * HTTP 方法
     */
    private String method;

    /**
     * 请求 URL
     */
    private String url;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求头
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * 查询参数
     */
    @Builder.Default
    private Map<String, Object> queryParams = new HashMap<>();

    /**
     * 路径变量
     */
    @Builder.Default
    private Map<String, Object> pathVariables = new HashMap<>();

    /**
     * 请求体
     */
    private Object body;

    /**
     * 添加请求头
     */
    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    /**
     * 添加查询参数
     */
    public void addQueryParam(String name, Object value) {
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        queryParams.put(name, value);
    }

    /**
     * 添加路径变量
     */
    public void addPathVariable(String name, Object value) {
        if (pathVariables == null) {
            pathVariables = new HashMap<>();
        }
        pathVariables.put(name, value);
    }

    /**
     * 构建完整的 URL
     */
    public String buildUrl() {
        String fullPath = path;

        // 替换路径变量
        if (pathVariables != null && !pathVariables.isEmpty()) {
            for (Map.Entry<String, Object> entry : pathVariables.entrySet()) {
                fullPath = fullPath.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        // 拼接查询参数
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder sb = new StringBuilder(fullPath);
            sb.append("?");
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            fullPath = sb.substring(0, sb.length() - 1);
        }

        // 如果有基础 URL，则拼接
        if (url != null && !url.isEmpty()) {
            if (url.endsWith("/") && fullPath.startsWith("/")) {
                return url + fullPath.substring(1);
            } else if (!url.endsWith("/") && !fullPath.startsWith("/")) {
                return url + "/" + fullPath;
            }
            return url + fullPath;
        }

        return fullPath;
    }
}
