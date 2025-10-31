package fun.hatsumi.tsumifeign.example.controller;

import fun.hatsumi.tsumifeign.example.client.UserClient;
import fun.hatsumi.tsumifeign.example.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试控制器
 *
 * @author hatsumi
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserClient userClient;

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Testing getUserById: {}", id);
        return userClient.getUserById(id);
    }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        log.info("Testing createUser: {}", user);
        return userClient.createUser(user);
    }

    @GetMapping("/users")
    public List<User> getUsers(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        log.info("Testing getUsers: page={}, size={}", page, size);
        return userClient.getUsers(page, size);
    }

    @PutMapping("/user/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("Testing updateUser: id={}, user={}", id, user);
        return userClient.updateUser(id, user);
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        log.info("Testing deleteUser: {}", id);
        userClient.deleteUser(id);
        return "User deleted successfully";
    }
}
