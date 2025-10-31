package fun.hatsumi.tsumifeigntest.user;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class userController {
    @GetMapping("/index")
    public String index() {
        return "index";
    }
    @GetMapping("{id}")
    public User getUser(@PathVariable Long id) {
        User user = new User();
        user.setId(0L);
        user.setName("");
        user.setEmail("");
        user.setAge(0);

        return user;

    }
    @PostMapping("/createUser")
    public String createUser() {
        return "createUser";
    }
}
