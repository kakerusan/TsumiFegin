package fun.hatsumi.tsumifeign.seata.client;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import fun.hatsumi.tsumifeign.seata.configuration.TsumiFeignSeataProperties;
import io.seata.core.context.RootContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SeataFeignClient 单元测试
 *
 * @author kakeru
 */
@ExtendWith(MockitoExtension.class)
class SeataFeignClientTest {

    @Mock
    private FeignClient delegateFeignClient;

    private SeataFeignClient seataFeignClient;
    private TsumiFeignSeataProperties properties;

    @BeforeEach
    void setUp() {
        properties = new TsumiFeignSeataProperties();
        properties.setEnabled(true);
        properties.setXidHeaderName("TX_XID");
        properties.setBranchTypeHeaderName("TX_BRANCH_TYPE");
        properties.setLogXid(false);

        seataFeignClient = new SeataFeignClient(delegateFeignClient, properties);
    }

    @AfterEach
    void tearDown() {
        // 清理Seata上下文，避免影响其他测试
        RootContext.unbind();
        RootContext.unbindBranchType();
    }

    @Test
    void testExecuteWithoutXid() throws IOException {
        // Given: 没有全局事务XID
        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        Response mockResponse = Response.builder()
                .status(200)
                .reason("OK")
                .body(new byte[0])
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenReturn(mockResponse);

        // When: 执行请求
        Response response = seataFeignClient.execute(requestTemplate);

        // Then: 请求成功，但不包含XID Header
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertFalse(requestTemplate.getHeaders().containsKey("TX_XID"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }

    @Test
    void testExecuteWithXid() throws IOException {
        // Given: 存在全局事务XID
        String xid = UUID.randomUUID().toString();
        RootContext.bind(xid);

        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        Response mockResponse = Response.builder()
                .status(200)
                .reason("OK")
                .body(new byte[0])
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenReturn(mockResponse);

        // When: 执行请求
        Response response = seataFeignClient.execute(requestTemplate);

        // Then: 请求成功，且包含XID Header
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(requestTemplate.getHeaders().containsKey("TX_XID"));
        assertEquals(xid, requestTemplate.getHeaders().get("TX_XID"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }

    @Test
    void testExecuteWithBranchType() throws IOException {
        // Given: 存在XID和分支类型
        String xid = UUID.randomUUID().toString();
        RootContext.bind(xid);
        RootContext.bindBranchType(io.seata.core.model.BranchType.AT);

        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        Response mockResponse = Response.builder()
                .status(200)
                .reason("OK")
                .body(new byte[0])
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenReturn(mockResponse);

        // When: 执行请求
        Response response = seataFeignClient.execute(requestTemplate);

        // Then: 请求成功，且包含XID和分支类型Header
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(requestTemplate.getHeaders().containsKey("TX_XID"));
        assertEquals(xid, requestTemplate.getHeaders().get("TX_XID"));
        assertTrue(requestTemplate.getHeaders().containsKey("TX_BRANCH_TYPE"));
        assertEquals("AT", requestTemplate.getHeaders().get("TX_BRANCH_TYPE"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }

    @Test
    void testExecuteWithException() throws IOException {
        // Given: 执行请求时抛出异常
        String xid = UUID.randomUUID().toString();
        RootContext.bind(xid);

        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenThrow(new IOException("Network error"));

        // When & Then: 执行请求应抛出异常
        assertThrows(IOException.class, () -> {
            seataFeignClient.execute(requestTemplate);
        });

        // 验证XID已添加到Header
        assertTrue(requestTemplate.getHeaders().containsKey("TX_XID"));
        assertEquals(xid, requestTemplate.getHeaders().get("TX_XID"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }

    @Test
    void testExecuteWithServerError() throws IOException {
        // Given: 服务器返回5xx错误
        String xid = UUID.randomUUID().toString();
        RootContext.bind(xid);

        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        Response mockResponse = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .body(new byte[0])
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenReturn(mockResponse);

        // When: 执行请求
        Response response = seataFeignClient.execute(requestTemplate);

        // Then: 请求成功返回，包含XID Header
        assertNotNull(response);
        assertEquals(503, response.getStatus());
        assertTrue(requestTemplate.getHeaders().containsKey("TX_XID"));
        assertEquals(xid, requestTemplate.getHeaders().get("TX_XID"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }

    @Test
    void testCustomHeaderNames() throws IOException {
        // Given: 自定义Header名称
        properties.setXidHeaderName("CUSTOM_XID");
        properties.setBranchTypeHeaderName("CUSTOM_BRANCH_TYPE");
        seataFeignClient = new SeataFeignClient(delegateFeignClient, properties);

        String xid = UUID.randomUUID().toString();
        RootContext.bind(xid);
        RootContext.bindBranchType(io.seata.core.model.BranchType.AT);

        RequestTemplate requestTemplate = RequestTemplate.builder()
                .url("http://localhost:8080")
                .method("POST")
                .path("/api/test")
                .build();

        Response mockResponse = Response.builder()
                .status(200)
                .reason("OK")
                .body(new byte[0])
                .build();

        when(delegateFeignClient.execute(any(RequestTemplate.class)))
                .thenReturn(mockResponse);

        // When: 执行请求
        Response response = seataFeignClient.execute(requestTemplate);

        // Then: 使用自定义Header名称
        assertNotNull(response);
        assertTrue(requestTemplate.getHeaders().containsKey("CUSTOM_XID"));
        assertEquals(xid, requestTemplate.getHeaders().get("CUSTOM_XID"));
        assertTrue(requestTemplate.getHeaders().containsKey("CUSTOM_BRANCH_TYPE"));
        assertEquals("AT", requestTemplate.getHeaders().get("CUSTOM_BRANCH_TYPE"));
        verify(delegateFeignClient, times(1)).execute(requestTemplate);
    }
}
