package fun.hatsumi.tsumifeign.contract;

import fun.hatsumi.tsumifeign.annotation.*;
import fun.hatsumi.tsumifeign.core.MethodMetadata;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * AnnotationContract 单元测试
 *
 * @author Kakeru
 */
public class AnnotationContractTest {

    private AnnotationContract contract;

    @Before
    public void setUp() {
        contract = new AnnotationContract();
    }

    @Test
    public void testParseGetMapping() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("getUser", Long.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals("GET", metadata.getHttpMethod());
        assertEquals("/users/{id}", metadata.getPath());
        assertEquals(method, metadata.getMethod());
        assertNotNull(metadata.getParameters());
        assertEquals(1, metadata.getParameters().size());

        MethodMetadata.ParameterMetadata param = metadata.getParameters().get(0);
        assertEquals(MethodMetadata.ParameterType.PATH, param.getParamType());
        assertEquals("id", param.getName());
    }

    @Test
    public void testParsePostMapping() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("createUser", TestUser.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals("POST", metadata.getHttpMethod());
        assertEquals("/users", metadata.getPath());
        assertEquals(1, metadata.getParameters().size());

        MethodMetadata.ParameterMetadata param = metadata.getParameters().get(0);
        assertEquals(MethodMetadata.ParameterType.BODY, param.getParamType());
    }

    @Test
    public void testParsePutMapping() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("updateUser", Long.class, TestUser.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals("PUT", metadata.getHttpMethod());
        assertEquals("/users/{id}", metadata.getPath());
        assertEquals(2, metadata.getParameters().size());
    }

    @Test
    public void testParseDeleteMapping() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("deleteUser", Long.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals("DELETE", metadata.getHttpMethod());
        assertEquals("/users/{id}", metadata.getPath());
    }

    @Test
    public void testParseRequestParam() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("searchUsers", String.class, Integer.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals(2, metadata.getParameters().size());

        MethodMetadata.ParameterMetadata param1 = metadata.getParameters().get(0);
        assertEquals(MethodMetadata.ParameterType.QUERY, param1.getParamType());
        assertEquals("name", param1.getName());

        MethodMetadata.ParameterMetadata param2 = metadata.getParameters().get(1);
        assertEquals(MethodMetadata.ParameterType.QUERY, param2.getParamType());
        assertEquals("age", param2.getName());
    }

    @Test
    public void testParseRequestHeader() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("getUserWithAuth", String.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata);
        assertEquals(1, metadata.getParameters().size());

        MethodMetadata.ParameterMetadata param = metadata.getParameters().get(0);
        assertEquals(MethodMetadata.ParameterType.HEADER, param.getParamType());
        assertEquals("Authorization", param.getName());
    }

    @Test
    public void testParseHeaders() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("getUser", Long.class);
        MethodMetadata metadata = contract.parseMethod(method);

        assertNotNull(metadata.getHeaders());
        assertEquals(1, metadata.getHeaders().length);
        assertEquals("Accept: application/json", metadata.getHeaders()[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void testParseMethodWithoutHttpAnnotation() throws NoSuchMethodException {
        Method method = TestClient.class.getMethod("invalidMethod");
        contract.parseMethod(method);
    }

    /**
     * 测试用接口
     */
    interface TestClient {
        @GetMapping(value = "/users/{id}", headers = "Accept: application/json")
        TestUser getUser(@PathVariable("id") Long id);

        @PostMapping("/users")
        TestUser createUser(@RequestBody TestUser user);

        @PutMapping("/users/{id}")
        TestUser updateUser(@PathVariable("id") Long id, @RequestBody TestUser user);

        @DeleteMapping("/users/{id}")
        void deleteUser(@PathVariable("id") Long id);

        @GetMapping("/users/search")
        TestUser searchUsers(@RequestParam("name") String name, @RequestParam("age") Integer age);

        @GetMapping("/users/current")
        TestUser getUserWithAuth(@RequestHeader("Authorization") String token);

        // 无 HTTP 方法注解的方法
        void invalidMethod();
    }

    /**
     * 测试用 POJO
     */
    static class TestUser {
        private Long id;
        private String name;

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
    }
}
