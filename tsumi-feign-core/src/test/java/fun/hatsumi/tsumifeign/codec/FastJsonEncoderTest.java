package fun.hatsumi.tsumifeign.codec;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * FastJsonEncoder 单元测试
 *
 * @author Kakeru
 */
public class FastJsonEncoderTest {

    private final FastJsonEncoder encoder = new FastJsonEncoder();

    @Test
    public void testEncodeNull() {
        byte[] result = encoder.encode(null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testEncodeString() {
        String input = "Hello TsumiFeign";
        byte[] result = encoder.encode(input);
        assertNotNull(result);
        String decoded = new String(result, StandardCharsets.UTF_8);
        assertEquals(input, decoded);
    }

    @Test
    public void testEncodeByteArray() {
        byte[] input = "test bytes".getBytes(StandardCharsets.UTF_8);
        byte[] result = encoder.encode(input);
        assertNotNull(result);
        assertArrayEquals(input, result);
    }

    @Test
    public void testEncodeObject() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "TsumiFeign");
        map.put("version", "1.0");
        map.put("age", 18);
        map.put("active", true);

        byte[] result = encoder.encode(map);
        assertNotNull(result);
        assertTrue(result.length > 0);

        String json = new String(result, StandardCharsets.UTF_8);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("TsumiFeign"));
    }

    @Test
    public void testGetContentType() {
        String contentType = encoder.getContentType();
        assertEquals("application/json; charset=UTF-8", contentType);
    }
}
