# TsumiFeign 客户端集成指南

## 📚 装饰器链架构

TsumiFeign 采用**装饰器模式**实现多功能集成，各模块按以下顺序叠加：

```
httpFeignClient (基础HTTP客户端)
    ↓
LoadBalancerFeignClient (Nacos服务发现)
    ↓
SentinelFeignClient (熔断降级)
    ↓
SeataFeignClient (分布式事务)
```

---

## 🔧 配置方式

### 1️⃣ 自动模式（推荐）

框架会根据引入的依赖自动构建最优装饰器链：

```yaml
tsumi:
  feign:
    client:
      default-client-type: auto  # 默认值
```

**效果：**
- 引入 `tsumi-feign-seata` → 自动启用完整链路
- 引入 `tsumi-feign-sentinel` + `tsumi-feign-nacos` → sentinel包装loadBalancer
- 仅引入 `tsumi-feign-nacos` → 仅启用服务发现
- 仅引入 `tsumi-feign-core` → 仅使用基础HTTP

---

### 2️⃣ 手动指定模式

通过配置显式指定客户端类型：

```yaml
tsumi:
  feign:
    client:
      default-client-type: sentinel  # 可选: http, loadBalancer, sentinel, seata
```

**注意：** 手动指定时需确保依赖已引入，否则启动失败。

---

### 3️⃣ 注解级别控制

在 `@TsumiFeignClient` 中指定特定接口的客户端类型：

```java
@TsumiFeignClient(name = "user-service", clientType = "sentinel")
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
```

**优先级：** 注解级 > 全局配置 > auto

---

## ✅ 各模块依赖配置

### 基础HTTP客户端

```xml
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-spring-cloud-starter</artifactId>
</dependency>
```

```yaml
tsumi:
  feign:
    client:
      default-client-type: http
```

---

### Nacos服务发现集成

```xml
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-nacos</artifactId>
</dependency>
```

```yaml
tsumi:
  feign:
    client:
      default-client-type: loadBalancer  # 或使用 auto

spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

---

### Sentinel熔断降级集成

```xml
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-nacos</artifactId> <!-- 可选，支持服务发现 -->
</dependency>
```

```yaml
tsumi:
  feign:
    client:
      default-client-type: sentinel  # 或使用 auto

spring:
  cloud:
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
```

**装饰器链：** `SentinelFeignClient` → `LoadBalancerFeignClient`(如有) → `httpFeignClient`

---

### Seata分布式事务集成

```xml
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-seata</artifactId>
</dependency>
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-sentinel</artifactId> <!-- 可选，支持熔断 -->
</dependency>
<dependency>
    <groupId>fun.hatsumi</groupId>
    <artifactId>tsumi-feign-nacos</artifactId> <!-- 可选，支持服务发现 -->
</dependency>
```

```yaml
tsumi:
  feign:
    client:
      default-client-type: seata  # 或使用 auto
    seata:
      enabled: true

seata:
  tx-service-group: default_tx_group
  service:
    vgroup-mapping:
      default_tx_group: default
```

**装饰器链：** `SeataFeignClient` → `SentinelFeignClient`(如有) → `LoadBalancerFeignClient`(如有) → `httpFeignClient`

---

## ⚠️ 注意事项

### 1. 避免@Primary冲突
✅ **已优化：** 各模块不再使用`@Primary`，通过`@AutoConfigureAfter`和智能选择机制协作

### 2. 模块加载顺序
框架通过以下机制确保正确的装饰顺序：
```java
@AutoConfigureAfter(name = {
    "fun.hatsumi.tsumifeign.spring.configuration.TsumiFeignAutoConfiguration",
    "fun.hatsumi.tsumifeign.nacos.configuration.TsumiFeignNacosAutoConfiguration",
    "fun.hatsumi.tsumifeign.sentinel.configuration.TsumiFeignSentinelAutoConfiguration"
})
```

### 3. 委托对象注入
使用 `@Lazy FeignClient delegate` 实现延迟注入，Spring会根据Bean定义顺序自动注入前置装饰器。

### 4. 配置开关控制
```yaml
# 禁用Sentinel（即使引入了依赖）
spring:
  cloud:
    sentinel:
      enabled: false

# 禁用Seata
tsumifeign:
  seata:
    enabled: false
```

---

## 🧪 验证装饰器链

启动应用时，观察日志输出：

```
INFO  - Creating httpFeignClient (OkHttpFeignClient)
INFO  - Creating LoadBalancerFeignClient with Nacos integration
INFO  - Creating SentinelFeignClient with fallback support
INFO  - Delegating to: LoadBalancerFeignClient
INFO  - Creating SeataFeignClient with transaction propagation
INFO  - Delegating to: SentinelFeignClient
INFO  - Auto-selected seataFeignClient (with transaction propagation)
```

---

## 📖 最佳实践

1. **生产环境推荐配置：**
   ```yaml
   tsumi:
     feign:
       client:
         default-client-type: auto  # 自动选择
   ```

2. **按需启用功能：**
   - 微服务调用 → 引入 `tsumi-feign-nacos`
   - 需要熔断 → 额外引入 `tsumi-feign-sentinel`
   - 分布式事务 → 额外引入 `tsumi-feign-seata`

3. **特定接口定制：**
   ```java
   // 大部分接口使用auto，特定接口直连HTTP
   @TsumiFeignClient(name = "legacy-api", clientType = "http", url = "http://old-service:8080")
   public interface LegacyApiClient {
       // ...
   }
   ```

---

## 🔗 相关文档

- [Nacos负载均衡配置](../tsumi-feign-nacos/README.md)
- [Sentinel规则配置](../tsumi-feign-sentinel/README.md)
- [Seata事务传播](../tsumi-feign-seata/README.md)
