# TsumiFeign

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-brightgreen)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> 🚀 一个轻量级、高性能的声明式 HTTP 客户端框架，深度集成 Spring Cloud 生态

TsumiFeign 是一个现代化的 Feign 风格 RPC 框架，学生学习使用，提供声明式 HTTP 调用能力，并与 Spring Cloud 生态无缝集成。支持 Nacos 服务发现、Sentinel 熔断降级、Seata 分布式事务等企业级特性。


## ✨ 核心特性

- 🎯 **声明式调用** - 使用注解定义接口，自动生成 HTTP 客户端
- 🔍 **服务发现** - 集成 Nacos，支持客户端负载均衡
- 🛡️ **熔断降级** - 集成 Sentinel，提供限流、熔断、降级能力
- 💼 **分布式事务** - 集成 Seata AT 模式，实现全局事务透明传播
- ⚡ **高性能** - 基于 OkHttp 4.x，连接池复用，性能优异
- 📦 **序列化支持** - 内置 FastJSON2 支持，可扩展 Protobuf
- 🔧 **Spring Boot 集成** - 自动装配，开箱即用

## 📦 模块说明


| 模块                               | 说明          | 核心功能                            |
| ---------------------------------- | ------------- | ----------------------------------- |
| `tsumi-feign-core`                 | 核心模块      | 注解、编码器、代理机制、HTTP 客户端 |
| `tsumi-feign-spring-cloud-starter` | Spring 集成   | 自动装配、FactoryBean、扫描注册     |
| `tsumi-feign-nacos`                | Nacos 集成    | 服务发现、负载均衡                  |
| `tsumi-feign-sentinel`             | Sentinel 集成 | 熔断降级、限流、Fallback 工厂       |
| `tsumi-feign-seata`                | Seata 集成    | 分布式事务、XID 传播、拦截器        |

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- Spring Boot 3.2.0+

### 添加依赖

在 `pom.xml` 中添加：

```xml
<dependencies>
    <!-- TsumiFeign Spring Cloud Starter -->
    <dependency>
        <groupId>fun.hatsumi</groupId>
        <artifactId>tsumi-feign-spring-cloud-starter</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
  
    <!-- Nacos 服务发现（可选） -->
    <dependency>
        <groupId>fun.hatsumi</groupId>
        <artifactId>tsumi-feign-nacos</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
  
    <!-- Sentinel 熔断降级（可选） -->
    <dependency>
        <groupId>fun.hatsumi</groupId>
        <artifactId>tsumi-feign-sentinel</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
  
    <!-- Seata 分布式事务（可选） -->
    <dependency>
        <groupId>fun.hatsumi</groupId>
        <artifactId>tsumi-feign-seata</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 定义客户端接口

```java
import fun.hatsumi.tsumifeign.annotation.*;

@TsumiFeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserFeignClient {
  
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
  
    @PostMapping("/api/users")
    User createUser(@RequestBody User user);
  
    @PutMapping("/api/users/{id}")
    void updateUser(@PathVariable("id") Long id, @RequestBody User user);
  
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
```

### 启用 TsumiFeign

在启动类上添加 `@EnableTsumiFeignClients` 注解：

```java
@SpringBootApplication
@EnableTsumiFeignClients
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 使用客户端

```java
@Service
public class UserService {
  
    @Autowired
    private UserFeignClient userFeignClient;
  
    public User getUser(Long id) {
        return userFeignClient.getUserById(id);
    }
}
```

## 🔧 配置说明

### 基础配置

```yaml
server:
  port: 8080

spring:
  application:
    name: my-service
```

### Nacos 服务发现

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: public
        group: DEFAULT_GROUP
```

使用服务名调用：

```java
@TsumiFeignClient(name = "user-service")  // 无需指定 URL
public interface UserFeignClient {
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
```

### Sentinel 熔断降级

```yaml
spring:
  cloud:
    sentinel:
      enabled: true
      transport:
        dashboard: 127.0.0.1:8080
        port: 8719

tsumifeign:
  sentinel:
    enabled: true
    resource-prefix: feign
```

自定义降级逻辑：

```java
@Component
public class UserFallbackFactory implements FallbackFactory<User> {
  
    @Override
    public Response create(Throwable throwable) {
        return Response.builder()
                .status(200)
                .reason("Fallback")
                .body(new User("fallback", "fallback@example.com"))
                .build();
    }
}
```

### Seata 分布式事务

```yaml
seata:
  enabled: true
  application-id: ${spring.application.name}
  tx-service-group: default_tx_group
  
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: seata
      group: SEATA_GROUP
  
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: seata
      group: SEATA_GROUP

tsumifeign:
  seata:
    enabled: true
    xid-header-name: TX_XID
    log-xid: false
```

使用全局事务：

```java
@Service
public class OrderService {
  
    @Autowired
    private AccountFeignClient accountFeignClient;
  
    @GlobalTransactional(
        name = "create-order",
        rollbackFor = Exception.class
    )
    public void createOrder(OrderRequest request) {
        // 本地数据库操作
        orderMapper.insert(order);
      
        // 远程调用（自动传播事务 XID）
        accountFeignClient.deductBalance(request.getUserId(), request.getAmount());
    }
}
```

## 🎨 高级特性

### 自定义编码器/解码器

```java
@Configuration
public class FeignConfig {
  
    @Bean
    public Encoder protobufEncoder() {
        return new ProtobufEncoder();
    }
  
    @Bean
    public Decoder protobufDecoder() {
        return new ProtobufDecoder();
    }
}
```

### 请求拦截器

```java
@Component
public class AuthInterceptor implements RequestInterceptor {
  
    @Override
    public void apply(RequestTemplate template) {
        String token = SecurityContextHolder.getContext().getToken();
        template.header("Authorization", "Bearer " + token);
    }
}
```

### 负载均衡策略

TsumiFeign 集成 Spring Cloud LoadBalancer，支持多种负载均衡策略：

- **RoundRobin**（默认）：轮询
- **Random**：随机
- **Nacos Weight**：基于 Nacos 权重

```yaml
spring:
  cloud:
    loadbalancer:
      nacos:
        enabled: true
```

## 📊 架构设计

### 调用链路

```
@TsumiFeignClient
        ↓
FeignInvocationHandler (动态代理)
        ↓
SeataFeignClient (分布式事务)
        ↓
SentinelFeignClient (熔断降级)
        ↓
LoadBalancerFeignClient (负载均衡)
        ↓
OkHttpFeignClient (HTTP 执行)
```

### 装饰器模式

TsumiFeign 采用装饰器模式，各功能模块可灵活组合：

- **核心层**：`OkHttpFeignClient` - HTTP 执行
- **负载均衡层**：`LoadBalancerFeignClient` - 服务发现与路由
- **熔断层**：`SentinelFeignClient` - 限流、熔断、降级
- **事务层**：`SeataFeignClient` - 全局事务传播


## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 使用 Java 21 特性
- 遵循 alibaba  代码风格
- 所有 public API 必须有文档注释

## 📝 版本历史

### v1.0.0 (2025-11-23)

- ✨ 初始版本发布
- ✅ 核心功能实现
- ✅ Nacos 服务发现集成
- ✅ Sentinel 熔断降级集成
- ✅ Seata 分布式事务集成

## 🛠️ 技术栈

- **核心框架**：Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **HTTP 客户端**：OkHttp 4.12.0
- **序列化**：FastJSON2 2.0.60
- **服务发现**：Nacos 2.x
- **熔断降级**：Sentinel 1.8.x
- **分布式事务**：Seata 1.8.0
- **日志框架**：SLF4J 2.0.9

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

⭐ 如果这个项目对你有帮助，请给个 Star！
