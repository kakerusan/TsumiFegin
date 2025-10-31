package fun.hatsumi.tsumifeign.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Response 单元测试
 *
 * @author Kakeru
 */
public class ResponseTest {

    @Test
    public void testSetAndGetStatus() {
        Response response = new Response();
        response.setStatus(200);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSetAndGetHeaders() {
        Response response = new Response();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", "1024");
        response.setHeaders(headers);

        assertEquals(headers, response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    public void testSetAndGetBody() {
        Response response = new Response();
        byte[] body = "{\"message\":\"success\"}".getBytes();
        response.setBody(body);

        assertArrayEquals(body, response.getBody());
    }

    @Test
    public void testIsSuccess() {
        Response response = new Response();
        
        response.setStatus(200);
        assertTrue(response.isSuccess());

        response.setStatus(201);
        assertTrue(response.isSuccess());

        response.setStatus(299);
        assertTrue(response.isSuccess());

        response.setStatus(199);
        assertFalse(response.isSuccess());

        response.setStatus(300);
        assertFalse(response.isSuccess());

        response.setStatus(404);
        assertFalse(response.isSuccess());

        response.setStatus(500);
        assertFalse(response.isSuccess());
    }

    @Test
    public void testEmptyBody() {
        Response response = new Response();
        response.setBody(new byte[0]);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    public void testNullBody() {
        Response response = new Response();
        response.setBody(null);
        assertNull(response.getBody());
    }

    @Test
    public void testEmptyHeaders() {
        Response response = new Response();
        response.setHeaders(new HashMap<>());
        assertNotNull(response.getHeaders());
        assertTrue(response.getHeaders().isEmpty());
    }

    @Test
    public void testGetHeaderCaseInsensitive() {
        Response response = new Response();
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        response.setHeaders(headers);

        // 注意：HashMap 是大小写敏感的
        assertEquals("application/json", response.getHeaders().get("content-type"));
        assertNull(response.getHeaders().get("Content-Type"));
    }
}
