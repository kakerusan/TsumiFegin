package fun.hatsumi.tsumifeign.integration;

import fun.hatsumi.tsumifeign.annotation.*;
import fun.hatsumi.tsumifeign.client.OkHttpFeignClient;
import fun.hatsumi.tsumifeign.codec.FastJsonDecoder;
import fun.hatsumi.tsumifeign.codec.FastJsonEncoder;
import fun.hatsumi.tsumifeign.contract.AnnotationContract;
import fun.hatsumi.tsumifeign.core.MethodMetadata;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Feign 集成测试
 * 测试完整的请求-响应流程
 *
 * @author Kakeru
 */
public class FeignIntegrationTest {

    private MockWebServer mockWebServer;
    private OkHttpFeignClient feignClient;
    private FastJsonEncoder encoder;
    private FastJsonDecoder decoder;
    private AnnotationContract contract;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        // 启动 Mock 服务器
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/api").toString();

        // 创建组件
        feignClient = new OkHttpFeignClient();
        encoder = new FastJsonEncoder();
        decoder = new FastJsonDecoder();
        contract = new AnnotationContract();
    }

    @After
    public void tearDown() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    public void testGetRequest() throws Exception {
        // 准备 Mock 响应
        String responseBody = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // 手动执行完整流程
        TestUser result = executeGetUser(1L);

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/users/1", request.getPath());

        // 验证响应
        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    private TestUser executeGetUser(Long id) throws IOException {
        // 解析方法
        Method method = null;
        try {
            method = TestClient.class.getMethod("getUser", Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        MethodMetadata metadata = contract.parseMethod(method);

        // 构建请求
        RequestTemplate template = RequestTemplate.builder()
                .method(metadata.getHttpMethod())
                .url(baseUrl)
                .path(metadata.getPath())
                .build();
        template.addPathVariable("id", id);

        // 执行请求
        Response response = feignClient.execute(template);

        // 解码响应
        return (TestUser) decoder.decode(response.getBody(), TestUser.class);
    }

    @Test
    public void testPostRequest() throws Exception {
        // 准备 Mock 响应
        String responseBody = "{\"id\":2,\"name\":\"New User\",\"email\":\"new@example.com\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // 执行请求
        TestUser newUser = new TestUser();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        TestUser result = executeCreateUser(newUser);

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/users", request.getPath());
        assertTrue(request.getBody().readUtf8().contains("New User"));

        // 验证响应
        assertNotNull(result);
        assertEquals(Long.valueOf(2), result.getId());
        assertEquals("New User", result.getName());
    }

    private TestUser executeCreateUser(TestUser user) throws IOException {
        Method method = null;
        try {
            method = TestClient.class.getMethod("createUser", TestUser.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        MethodMetadata metadata = contract.parseMethod(method);

        RequestTemplate template = RequestTemplate.builder()
                .method(metadata.getHttpMethod())
                .url(baseUrl)
                .path(metadata.getPath())
                .body(encoder.encode(user))
                .build();
        template.addHeader("Content-Type", encoder.getContentType());

        Response response = feignClient.execute(template);
        return (TestUser) decoder.decode(response.getBody(), TestUser.class);
    }

    @Test
    public void testCodecIntegration() throws Exception {
        // 测试编解码器集成
        String responseBody = "{\"id\":3,\"name\":\"Codec Test\",\"email\":\"codec@test.com\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        TestUser result = executeGetUser(3L);

        assertNotNull(result);
        assertEquals(Long.valueOf(3), result.getId());
        assertEquals("Codec Test", result.getName());
    }

    /**
     * 测试用客户端接口
     */
    @TsumiFeignClient(name = "test-service", url = "http://localhost:8080")
    interface TestClient {
        @GetMapping("/users/{id}")
        TestUser getUser(@PathVariable("id") Long id);

        @PostMapping("/users")
        TestUser createUser(@RequestBody TestUser user);

        @PutMapping("/users/{id}")
        TestUser updateUser(@PathVariable("id") Long id, @RequestBody TestUser user);

        @DeleteMapping("/users/{id}")
        void deleteUser(@PathVariable("id") Long id);

        @GetMapping("/users/search")
        List<TestUser> searchUsers(@RequestParam("name") String name, @RequestParam("age") Integer age);

        @GetMapping("/users/current")
        TestUser getUserWithAuth(@RequestHeader("Authorization") String token);
    }

    /**
     * 测试用 POJO
     */
    public static class TestUser {
        private Long id;
        private String name;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
