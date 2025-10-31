package fun.hatsumi.tsumifeign.example;

import fun.hatsumi.tsumifeign.factory.TsumiFeignClientFactory;

/**
 * 使用示例
 *
 * @author Kakeru
 */
public class Example {

    public static void main(String[] args) {
        System.out.println("=== TsumiFeign 示例程序 ===");
        System.out.println();

        // 创建 Feign 客户端
        System.out.println("1. 创建 Feign 客户端...");
        UserServiceClient userServiceClient = TsumiFeignClientFactory.create(UserServiceClient.class);
        System.out.println("✓ 客户端创建成功: " + userServiceClient.getClass().getName());
        System.out.println();

        // 示例：获取用户
        System.out.println("2. 尝试调用 GET /users/1...");
        try {
            User userById = userServiceClient.getUserById(1L);
            System.out.println("✓ 获取用户成功: " + userById);

        } catch (Exception e) {
            System.err.println("✗ 服务不可用 (http://localhost:8080 未启动): " + e.getClass().getSimpleName());
            System.err.println("  错误信息: " + e.getMessage());
        }
        System.out.println();

        // 示例：创建用户
        System.out.println("3. 尝试调用 POST /users...");
        try {
            User newUser = User.builder()
                    .name("张三")
                    .email("zhangsan@example.com")
                    .age(25)
                    .build();
            System.out.println("  请求体: " + newUser);
            User created = userServiceClient.createUser(newUser);
            System.out.println("✓ 创建用户成功: " + created);
        } catch (Exception e) {
            System.err.println("✗ 服务不可用: " + e.getClass().getSimpleName());
        }
        System.out.println();

        // 示例：查询用户列表
        System.out.println("4. 尝试调用 GET /users?page=1&size=10...");
        try {
            var users = userServiceClient.getUsers(1, 10);
            System.out.println("✓ 获取用户列表成功, 数量: " + users.size());
        } catch (Exception e) {
            System.err.println("✗ 服务不可用: " + e.getClass().getSimpleName());
        }
        System.out.println();


    }
}
