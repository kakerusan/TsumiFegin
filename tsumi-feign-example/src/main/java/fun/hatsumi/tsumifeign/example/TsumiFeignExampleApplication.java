package fun.hatsumi.tsumifeign.example;

import fun.hatsumi.tsumifeign.spring.annotation.EnableTsumiFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TsumiFeign 示例应用
 *
 * @author hatsumi
 */
@SpringBootApplication
@EnableTsumiFeignClients
public class TsumiFeignExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsumiFeignExampleApplication.class, args);
    }
}
