package fun.hatsumi.tsumifeign;

import fun.hatsumi.tsumifeign.example.User;
import fun.hatsumi.tsumifeign.example.UserServiceClient;
import fun.hatsumi.tsumifeign.factory.TsumiFeignClientFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Feign 客户端测试
 *
 * @author hatsumi
 */
public class FeignClientTest {

    @Test
    public void testCreateClient() {
        // 创建客户端
        UserServiceClient client = TsumiFeignClientFactory.create(UserServiceClient.class);
        assertNotNull(client);
    }

    @Test
    public void testClientSingleton() {
        // 测试单例模式
        UserServiceClient client1 = TsumiFeignClientFactory.create(UserServiceClient.class);
        UserServiceClient client2 = TsumiFeignClientFactory.create(UserServiceClient.class);
        assertSame(client1, client2);
    }

    @Test
    public void testUserBuilder() {
        // 测试 User 对象构建
        User user = User.builder()
                .id(1L)
                .name("测试用户")
                .email("test@example.com")
                .age(25)
                .build();

        assertEquals(Long.valueOf(1L), user.getId());
        assertEquals("测试用户", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(Integer.valueOf(25), user.getAge());
    }
}
