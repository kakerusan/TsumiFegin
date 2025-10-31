package fun.hatsumi.tsumifeign.example;

import fun.hatsumi.tsumifeign.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户实体类示例
 *
 * @author Kakeru
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long id;
    private String name;
    private String email;
    private Integer age;
}
