package fun.hatsumi.tsumifeign.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * RequestTemplate 单元测试
 *
 * @author Kakeru
 */
public class RequestTemplateTest {

    @Test
    public void testBuildUrlWithoutParams() {
        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users")
                .build();

        String url = template.buildUrl();
        assertEquals("http://localhost:8080/api/users", url);
    }

    @Test
    public void testBuildUrlWithPathVariables() {
        Map<String, Object> pathVariables = new HashMap<>();
        pathVariables.put("id", "123");
        pathVariables.put("orderId", "456");

        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users/{id}/orders/{orderId}")
                .pathVariables(pathVariables)
                .build();

        String url = template.buildUrl();
        assertEquals("http://localhost:8080/api/users/123/orders/456", url);
    }

    @Test
    public void testBuildUrlWithQueryParams() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "TsumiFeign");
        queryParams.put("age", "18");

        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users")
                .queryParams(queryParams)
                .build();

        String url = template.buildUrl();
        assertTrue(url.startsWith("http://localhost:8080/api/users?"));
        assertTrue(url.contains("name=TsumiFeign"));
        assertTrue(url.contains("age=18"));
    }

    @Test
    public void testBuildUrlWithPathVariablesAndQueryParams() {
        Map<String, Object> pathVariables = new HashMap<>();
        pathVariables.put("id", "123");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("expand", "orders");

        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users/{id}")
                .pathVariables(pathVariables)
                .queryParams(queryParams)
                .build();

        String url = template.buildUrl();
        assertTrue(url.startsWith("http://localhost:8080/api/users/123?"));
        assertTrue(url.contains("expand=orders"));
    }

    @Test
    public void testSetAndGetMethod() {
        RequestTemplate template = RequestTemplate.builder()
                .method("POST")
                .build();
        assertEquals("POST", template.getMethod());
    }

    @Test
    public void testSetAndGetHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token");

        RequestTemplate template = RequestTemplate.builder()
                .headers(headers)
                .build();

        assertEquals(headers, template.getHeaders());
        assertEquals("application/json", template.getHeaders().get("Content-Type"));
    }

    @Test
    public void testSetAndGetBody() {
        byte[] body = "{\"name\":\"test\"}".getBytes();
        RequestTemplate template = RequestTemplate.builder()
                .body(body)
                .build();

        assertArrayEquals(body, (byte[]) template.getBody());
    }

    @Test
    public void testUrlEncoding() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "张三");
        queryParams.put("email", "test@example.com");

        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users")
                .queryParams(queryParams)
                .build();

        String url = template.buildUrl();
        assertNotNull(url);
        assertTrue(url.contains("?"));
    }

    @Test
    public void testEmptyPath() {
        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("")
                .build();

        String url = template.buildUrl();
        // 空路径会返回 URL + "/"
        assertEquals("http://localhost:8080/api/", url);
    }

    @Test
    public void testNullQueryParams() {
        RequestTemplate template = RequestTemplate.builder()
                .url("http://localhost:8080/api")
                .path("/users")
                .queryParams(null)
                .build();

        String url = template.buildUrl();
        assertEquals("http://localhost:8080/api/users", url);
    }
}
