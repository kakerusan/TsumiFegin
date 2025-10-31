package fun.hatsumi.tsumifeign.example;

import fun.hatsumi.tsumifeign.annotation.*;

import java.util.List;

/**
 * 用户服务客户端示例
 *
 * @author Kakeru
 */
@TsumiFeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserServiceClient {

    /**
     * 根据 ID 获取用户
     */
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") Long id);

    /**
     * 创建用户
     */
    @PostMapping("/users")
    User createUser(@RequestBody User user);

    /**
     * 分页查询用户
     */
    @GetMapping("/users")
    List<User> getUsers(@RequestParam("page") int page,
                        @RequestParam("size") int size);

    /**
     * 更新用户
     */
    @PutMapping("/users/{id}")
    User updateUser(@PathVariable("id") Long id, @RequestBody User user);

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable("id") Long id);

    /**
     * 带请求头的示例
     */
    @GetMapping(value = "/users/profile", headers = {"Authorization:Bearer token"})
    User getProfile(@RequestHeader("User-Id") String userId);
}
