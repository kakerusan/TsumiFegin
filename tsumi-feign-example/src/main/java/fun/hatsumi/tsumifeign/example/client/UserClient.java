package fun.hatsumi.tsumifeign.example.client;

import fun.hatsumi.tsumifeign.annotation.*;
import fun.hatsumi.tsumifeign.example.model.User;

import java.util.List;

/**
 * 用户服务客户端
 *
 * @author hatsumi
 */
@TsumiFeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserClient {

    @GetMapping("/{id}")
    User getUserById(@PathVariable("id") Long id);

    @PostMapping
    User createUser(@RequestBody User user);

    @GetMapping
    List<User> getUsers(@RequestParam("page") int page,
                        @RequestParam("size") int size);

    @PutMapping("/{id}")
    User updateUser(@PathVariable("id") Long id, @RequestBody User user);

    @DeleteMapping("/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
