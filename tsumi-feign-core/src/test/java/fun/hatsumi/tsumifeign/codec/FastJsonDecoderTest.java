package fun.hatsumi.tsumifeign.codec;

import org.junit.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * FastJsonDecoder 单元测试
 *
 * @author Kakeru
 */
public class FastJsonDecoderTest {

    private final FastJsonDecoder decoder = new FastJsonDecoder();

    @Test
    public void testDecodeNull() {
        Object result = decoder.decode(null, String.class);
        assertNull(result);
    }

    @Test
    public void testDecodeEmptyBytes() {
        Object result = decoder.decode(new byte[0], String.class);
        assertNull(result);
    }

    @Test
    public void testDecodeString() {
        String input = "Hello TsumiFeign";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        Object result = decoder.decode(bytes, String.class);
        assertNotNull(result);
        assertEquals(input, result);
    }

    @Test
    public void testDecodeByteArray() {
        byte[] input = "test bytes".getBytes(StandardCharsets.UTF_8);
        Object result = decoder.decode(input, byte[].class);
        assertNotNull(result);
        assertArrayEquals(input, (byte[]) result);
    }

    @Test
    public void testDecodeVoid() {
        byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
        Object result = decoder.decode(bytes, void.class);
        assertNull(result);
    }

    @Test
    public void testDecodeVoidClass() {
        byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
        Object result = decoder.decode(bytes, Void.class);
        assertNull(result);
    }

    @Test
    public void testDecodeObject() {
        String json = "{\"name\":\"TsumiFeign\",\"version\":\"1.0\",\"age\":18}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        
        Object result = decoder.decode(bytes, Map.class);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals("TsumiFeign", map.get("name"));
        assertEquals("1.0", map.get("version"));
    }

    @Test
    public void testDecodeComplexObject() {
        String json = "{\"id\":1,\"name\":\"TestUser\",\"email\":\"test@example.com\",\"active\":true}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        
        Object result = decoder.decode(bytes, TestUser.class);
        assertNotNull(result);
        assertTrue(result instanceof TestUser);
        
        TestUser user = (TestUser) result;
        assertEquals(Long.valueOf(1), user.getId());
        assertEquals("TestUser", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isActive());
    }

    /**
     * 测试用 POJO
     */
    public static class TestUser {
        private Long id;
        private String name;
        private String email;
        private boolean active;

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

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
