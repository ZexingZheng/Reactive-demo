# 响应式 CRUD 项目面试准备指南

## 📋 目录
1. [项目概述](#项目概述)
2. [核心功能实现详解](#核心功能实现详解)
3. [面试重点考点](#面试重点考点)
4. [定制化改造指南](#定制化改造指南)
5. [常见面试问题及答案](#常见面试问题及答案)

---

## 项目概述

这是一个基于 **Spring WebFlux** 和 **Project Reactor** 的现代化响应式 CRUD 应用，展示了从 Web 层到数据层的完全非阻塞式编程模式。

### 技术栈
- **Web 层**: Spring WebFlux (响应式 Web 框架)
- **数据层**: R2DBC (响应式关系型数据库) + MongoDB (响应式 NoSQL)
- **缓存**: Redis (响应式缓存)
- **消息队列**: Kafka (事件驱动架构)
- **API 文档**: Swagger/OpenAPI 3.0
- **监控**: Spring Boot Actuator

---

## 核心功能实现详解

### 1. 响应式 Web 层 (Controller)

#### 关键文件位置
- `src/main/java/com/example/reactive/controller/UserController.java` - R2DBC 用户控制器
- `src/main/java/com/example/reactive/controller/UserMongoController.java` - MongoDB 用户控制器

#### 实现原理
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
```

#### 核心概念
- **Mono**: 0 或 1 个元素的响应式流
- **Flux**: 0 到 N 个元素的响应式流
- **非阻塞**: 不会阻塞调用线程，通过事件循环处理请求

#### 面试考点
1. **为什么返回 Mono/Flux 而不是对象？**
   - 答：支持异步非阻塞，提高吞吐量，适合高并发场景

2. **如何处理异常？**
   - 答：通过 GlobalExceptionHandler 全局异常处理器统一处理

3. **如何实现背压(Backpressure)？**
   - 答：Reactor 自动实现，消费者可以控制数据流速

---

### 2. 响应式服务层 (Service)

#### 关键文件位置
- `src/main/java/com/example/reactive/service/UserService.java` - R2DBC 用户服务
- `src/main/java/com/example/reactive/service/UserMongoService.java` - MongoDB 用户服务
- `src/main/java/com/example/reactive/service/EmailService.java` - 邮件服务

#### 实现原理 - CRUD + 事件发布
```java
public Mono<User> createUser(User user) {
    return userRepository.save(user)
        .flatMap(savedUser -> {
            // 异步发送 Kafka 事件
            UserEvent event = UserEvent.create(...);
            return kafkaProducer.sendUserEvent(event)
                .doOnError(e -> log.error("Kafka failed", e))
                .onErrorResume(e -> Mono.empty())
                .thenReturn(savedUser);
        });
}
```

#### 核心概念
- **flatMap**: 将一个响应式流转换为另一个，用于链式异步操作
- **doOnError**: 错误处理回调
- **onErrorResume**: 错误恢复，提供降级方案
- **thenReturn**: 忽略上游结果，返回指定值

#### 面试考点
1. **为什么 Kafka 发送失败不影响主流程？**
   - 答：使用 `onErrorResume(e -> Mono.empty())` 实现降级，保证核心业务不受影响

2. **如何实现事务？**
   - 答：R2DBC 支持 `@Transactional`，但需要注意响应式事务的边界

3. **缓存如何集成？**
   - 答：UserMongoService 使用 `@Cacheable`、`@CacheEvict` 注解，配合 Redis 实现

---

### 3. 响应式数据访问层 (Repository)

#### 关键文件位置
- `src/main/java/com/example/reactive/repository/UserRepository.java` - R2DBC 仓库
- `src/main/java/com/example/reactive/repository/UserMongoRepository.java` - MongoDB 仓库

#### 实现原理
```java
// R2DBC
public interface UserRepository extends R2dbcRepository<User, Long> {
    Flux<User> findByName(String name);
}

// MongoDB
public interface UserMongoRepository extends ReactiveMongoRepository<UserMongo, String> {
    Flux<UserMongo> findByNameContainingIgnoreCase(String name);
}
```

#### 核心概念
- **R2DBC**: Reactive Relational Database Connectivity，响应式关系型数据库驱动
- **ReactiveMongoRepository**: Spring Data MongoDB 的响应式仓库
- **自动方法推导**: Spring Data 根据方法名自动生成查询

#### 面试考点
1. **R2DBC 和 JDBC 的区别？**
   - 答：R2DBC 是完全非阻塞的，基于 Reactive Streams 规范；JDBC 是阻塞式的

2. **如何执行复杂查询？**
   - 答：使用 `@Query` 注解或 `R2dbcEntityTemplate` 进行自定义查询

3. **数据库连接池如何配置？**
   - 答：在 `application.yml` 中配置 `spring.r2dbc.pool` 相关参数

---

### 4. Kafka 响应式集成

#### 关键文件位置
- `src/main/java/com/example/reactive/kafka/ReactiveKafkaProducer.java` - 生产者
- `src/main/java/com/example/reactive/kafka/ReactiveKafkaConsumer.java` - 消费者
- `src/main/java/com/example/reactive/config/KafkaConfig.java` - 配置
- `src/main/java/com/example/reactive/event/UserEvent.java` - 事件模型

#### 实现原理 - 生产者
```java
public Mono<Void> sendUserEvent(UserEvent event) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
        .flatMap(json -> {
            ProducerRecord<String, String> record = 
                new ProducerRecord<>(TOPIC, event.getUserId().toString(), json);
            SenderRecord<String, String, String> senderRecord = 
                SenderRecord.create(record, event.getUserId().toString());
            
            return kafkaSender.send(Mono.just(senderRecord))
                .next()
                .doOnSuccess(result -> log.info("Sent to Kafka"))
                .then();
        });
}
```

#### 实现原理 - 消费者
```java
@PostConstruct
public void startConsuming() {
    disposable = kafkaReceiver.receive()
        .flatMap(this::processRecord)
        .doOnError(error -> log.error("Error consuming", error))
        .retry()
        .subscribe();
}

private Mono<Void> processRecord(ReceiverRecord<String, String> record) {
    return Mono.fromCallable(() -> objectMapper.readValue(record.value(), UserEvent.class))
        .flatMap(event -> processEvent(event)
            .doOnSuccess(v -> record.receiverOffset().acknowledge()));
}
```

#### 核心概念
- **Reactor Kafka**: 基于 Project Reactor 的 Kafka 客户端
- **手动确认**: 使用 `record.receiverOffset().acknowledge()` 控制消费进度
- **背压支持**: 自动处理消费速率
- **错误重试**: 使用 `retry()` 实现自动重试

#### 面试考点
1. **如何保证消息不丢失？**
   - 答：生产者配置 `acks=all`，消费者使用手动确认模式

2. **如何处理消费失败？**
   - 答：使用 `onErrorResume` 捕获异常，记录日志后确认消息（避免无限重试）

3. **如何实现消息顺序？**
   - 答：配置 `max.in.flight.requests.per.connection=1`，同一 key 发送到同一分区

---

### 5. Redis 缓存集成

#### 关键文件位置
- `src/main/java/com/example/reactive/config/RedisConfig.java` - Redis 配置
- `src/main/java/com/example/reactive/service/UserMongoService.java` - 缓存使用示例

#### 实现原理
```java
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        // ... 配置序列化
    }
}

// 使用缓存
@Cacheable(value = "users", key = "#id")
public Mono<UserMongo> getUserById(String id) {
    return userMongoRepository.findById(id);
}

@CacheEvict(value = "users", allEntries = true)
public Mono<UserMongo> createUser(UserMongo user) {
    return userMongoRepository.save(user);
}
```

#### 核心概念
- **@EnableCaching**: 启用 Spring 缓存抽象
- **@Cacheable**: 查询时使用缓存
- **@CacheEvict**: 更新/删除时清除缓存
- **TTL 配置**: 通过 `RedisCacheConfiguration` 设置过期时间

#### 面试考点
1. **缓存击穿/穿透/雪崩如何处理？**
   - 击穿：使用分布式锁
   - 穿透：缓存空值或使用布隆过滤器
   - 雪崩：设置随机过期时间

2. **为什么要清除所有缓存（allEntries = true）？**
   - 答：因为查询方法可能有多个缓存 key（如 by id、by name），为简化逻辑全部清除

---

### 6. 邮件异步发送

#### 关键文件位置
- `src/main/java/com/example/reactive/service/EmailService.java` - 邮件服务
- `src/main/java/com/example/reactive/config/EmailConfig.java` - 线程池配置
- `src/main/java/com/example/reactive/email/EmailMessage.java` - 邮件模型

#### 实现原理
```java
// 专用线程池
@Bean(name = "emailScheduler")
public Scheduler emailScheduler() {
    return Schedulers.fromExecutor(
        Executors.newFixedThreadPool(10, r -> {
            Thread thread = new Thread(r);
            thread.setName("email-sender-");
            return thread;
        })
    );
}

// 异步发送
public Mono<Void> sendEmail(EmailMessage emailMessage) {
    return Mono.fromCallable(() -> {
            sendEmailSync(emailMessage);
            return true;
        })
        .subscribeOn(emailScheduler)  // 在专用线程池执行
        .timeout(Duration.ofSeconds(30))
        .onErrorResume(e -> Mono.empty())  // 失败不影响主流程
        .then();
}
```

#### 核心概念
- **subscribeOn**: 指定执行线程池，将阻塞操作隔离
- **Scheduler**: Reactor 的线程调度器
- **阻塞 API 适配**: 将阻塞的 JavaMailSender 包装为响应式

#### 面试考点
1. **为什么需要单独的线程池？**
   - 答：JavaMailSender 是阻塞的，需要隔离到专用线程池，避免阻塞主事件循环

2. **如果邮件发送很慢怎么办？**
   - 答：设置 `timeout()`，超时后自动放弃，不影响主流程

3. **如何提高邮件发送性能？**
   - 答：增大线程池大小，使用消息队列异步发送，批量发送

---

### 7. 全局异常处理

#### 关键文件位置
- `src/main/java/com/example/reactive/exception/GlobalExceptionHandler.java` - 全局异常处理器
- `src/main/java/com/example/reactive/exception/ResourceNotFoundException.java` - 自定义异常

#### 实现原理
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
            WebExchangeBindException ex) {
        // 处理参数校验异常
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        // 处理资源不存在异常
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex) {
        // 处理通用异常
    }
}
```

#### 核心概念
- **@RestControllerAdvice**: 全局异常处理切面
- **@ExceptionHandler**: 指定处理的异常类型
- **统一错误格式**: 返回标准化的错误响应

#### 面试考点
1. **响应式和传统异常处理有什么区别？**
   - 答：返回 `Mono<ResponseEntity>` 而不是直接返回 ResponseEntity，保持响应式链路

2. **如何处理异步异常？**
   - 答：在响应式链中使用 `onErrorResume`、`doOnError` 等操作符

---

### 8. CORS 和静态资源配置

#### 关键文件位置
- `src/main/java/com/example/reactive/config/WebConfig.java` - Web 配置

#### 实现原理
```java
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }
}
```

#### 面试考点
1. **生产环境 CORS 如何配置？**
   - 答：不要用 `allowedOrigins("*")`，应指定具体域名

2. **如何处理预检请求（OPTIONS）？**
   - 答：Spring WebFlux 自动处理，配置 `allowedMethods` 包含 OPTIONS

---

### 9. API 文档 (Swagger/OpenAPI)

#### 关键文件位置
- Controller 类中的 `@Tag`、`@Operation` 注解
- `pom.xml` 中的 `springdoc-openapi-starter-webflux-ui` 依赖

#### 实现原理
```java
@Tag(name = "User R2DBC API", description = "User management R2DBC interface")
public class UserController {
    
    @Operation(summary = "Get all users", description = "Query all user list")
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
```

#### 访问地址
- Swagger UI: `http://localhost:9090/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9090/v3/api-docs`

#### 面试考点
1. **如何保护 Swagger 接口？**
   - 答：生产环境通过 Spring Security 限制访问，或使用 `@ConditionalOnProperty` 禁用

2. **如何自定义 API 文档？**
   - 答：使用 `@Schema` 注解描述模型，`@ApiResponse` 描述响应

---

### 10. 监控和健康检查 (Actuator)

#### 关键文件位置
- `pom.xml` 中的 `spring-boot-starter-actuator` 依赖
- `application.yml` 中的 actuator 配置

#### 访问端点
- Health: `http://localhost:9090/actuator/health`
- Metrics: `http://localhost:9090/actuator/metrics`
- Info: `http://localhost:9090/actuator/info`

#### 面试考点
1. **生产环境如何保护 Actuator 端点？**
   - 答：配置 `management.endpoints.web.exposure.include` 只暴露必要端点，使用 Spring Security 保护

2. **如何自定义健康检查？**
   - 答：实现 `HealthIndicator` 接口

---

## 面试重点考点

### 响应式编程基础

1. **什么是响应式编程？**
   - 异步非阻塞的编程范式
   - 基于数据流和变化传播
   - 支持背压机制

2. **Mono 和 Flux 的区别？**
   - Mono: 0-1 个元素
   - Flux: 0-N 个元素
   - 都实现了 Publisher 接口

3. **常用操作符**
   - 转换：map, flatMap, flatMapMany
   - 过滤：filter, take, skip
   - 组合：zip, merge, concat
   - 错误处理：onErrorResume, onErrorReturn, retry

### 性能优化

1. **如何提高响应式应用性能？**
   - 使用正确的线程调度器 (Schedulers)
   - 避免阻塞操作，必要时使用 subscribeOn 隔离
   - 合理使用缓存
   - 配置连接池大小

2. **如何监控性能？**
   - 使用 Actuator metrics
   - 集成 Prometheus + Grafana
   - 使用 Spring Cloud Sleuth 进行链路追踪

### 数据一致性

1. **分布式事务如何处理？**
   - 使用 Saga 模式
   - 基于消息的最终一致性
   - 幂等性设计

2. **缓存一致性如何保证？**
   - Cache-Aside 模式
   - 更新时删除缓存
   - 使用事件驱动刷新缓存

---

## 定制化改造指南

### 快速定位文件

| 功能模块 | 文件路径 | 用途 |
|---------|----------|------|
| R2DBC CRUD | `controller/UserController.java` | HTTP 接口定义 |
| MongoDB CRUD | `controller/UserMongoController.java` | MongoDB 接口 |
| 业务逻辑 | `service/UserService.java` | R2DBC 业务层 |
| MongoDB 业务 | `service/UserMongoService.java` | MongoDB 业务层 |
| Kafka 生产 | `kafka/ReactiveKafkaProducer.java` | 发送事件 |
| Kafka 消费 | `kafka/ReactiveKafkaConsumer.java` | 处理事件 |
| 邮件发送 | `service/EmailService.java` | 邮件功能 |
| 全局异常 | `exception/GlobalExceptionHandler.java` | 异常处理 |
| CORS 配置 | `config/WebConfig.java` | 跨域配置 |
| Redis 配置 | `config/RedisConfig.java` | 缓存配置 |
| Kafka 配置 | `config/KafkaConfig.java` | 消息队列配置 |

### 常见定制需求

#### 1. 添加新的 REST 接口

**位置**: `controller/UserController.java`

```java
@GetMapping("/active")
@Operation(summary = "Get active users", description = "Query active users only")
public Flux<User> getActiveUsers() {
    return userService.getActiveUsers();
}
```

**需要同步修改**:
- `service/UserService.java` - 添加业务方法
- `repository/UserRepository.java` - 添加查询方法（如果需要）

#### 2. 修改 Kafka 消息格式

**位置**: `event/UserEvent.java`

```java
@Data
public class UserEvent {
    private String eventType;
    private String userId;
    private String userName;
    // 添加新字段
    private String department;
    private String role;
}
```

**需要同步修改**:
- `kafka/ReactiveKafkaProducer.java` - 确保序列化正确
- `kafka/ReactiveKafkaConsumer.java` - 处理新字段
- `service/UserService.java` - 构造事件时填充新字段

#### 3. 更改缓存策略

**位置**: `config/RedisConfig.java`

```java
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30))  // 修改过期时间
        .disableCachingNullValues();
}
```

**位置**: `service/UserMongoService.java`

```java
@Cacheable(value = "users", key = "#id", unless = "#result == null")
public Mono<UserMongo> getUserById(String id) {
    return userMongoRepository.findById(id);
}
```

#### 4. 添加新的异常类型

**位置**: `exception/GlobalExceptionHandler.java`

```java
@ExceptionHandler(DuplicateUserException.class)
public Mono<ResponseEntity<Map<String, Object>>> handleDuplicateUserException(
        DuplicateUserException ex) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", HttpStatus.CONFLICT.value());
    errorResponse.put("error", "Duplicate User");
    errorResponse.put("message", ex.getMessage());
    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
}
```

#### 5. 修改 Kafka Topic

**位置**: `kafka/ReactiveKafkaProducer.java`

```java
private static final String TOPIC = "user-events-v2";  // 修改 topic 名称
```

**位置**: `config/KafkaConfig.java`

```java
@Bean
public ReceiverOptions<String, String> receiverOptions() {
    // ...
    return ReceiverOptions.<String, String>create(props)
        .subscription(Collections.singleton("user-events-v2"));  // 同步修改
}
```

#### 6. 修改邮件模板

**位置**: `email/EmailMessage.java`

```java
private static String buildWelcomeContent(String name) {
    return String.format("""
        <html>
        <body>
            <h2>Welcome, %s!</h2>
            <p>Your custom welcome message here...</p>
        </body>
        </html>
        """, name);
}
```

#### 7. 添加参数验证

**位置**: `entity/User.java`

```java
@Data
@Table("users")
public class User {
    @Id
    private Long id;
    
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;
}
```

**位置**: `controller/UserController.java`

```java
@PostMapping
public Mono<User> createUser(@Valid @RequestBody User user) {  // 添加 @Valid
    return userService.createUser(user);
}
```

#### 8. 修改数据库连接配置

**位置**: `src/main/resources/application.yml`

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/mydb  # 切换到 PostgreSQL
    username: postgres
    password: password
    pool:
      initial-size: 10
      max-size: 50
```

#### 9. 添加安全认证

**新增文件**: `config/SecurityConfig.java`

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/users/**").authenticated()
                .anyExchange().permitAll()
            )
            .httpBasic()
            .and()
            .csrf().disable()
            .build();
    }
}
```

#### 10. 添加限流功能

**新增文件**: `config/RateLimitConfig.java`

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100);  // 每秒 100 个请求
    }
}
```

**修改**: `controller/UserController.java`

```java
@Autowired
private RateLimiter rateLimiter;

@GetMapping
public Flux<User> getAllUsers() {
    return Mono.fromCallable(() -> rateLimiter.tryAcquire())
        .flatMapMany(acquired -> {
            if (!acquired) {
                return Flux.error(new RateLimitExceededException("Too many requests"));
            }
            return userService.getAllUsers();
        });
}
```

---

## 常见面试问题及答案

### Q1: 为什么选择响应式编程？

**答案**:
- **高并发处理能力**: 非阻塞 I/O，用少量线程处理大量请求
- **资源利用率高**: 避免线程阻塞等待，减少线程数量
- **背压支持**: 自动调节数据流速，防止系统过载
- **适合 I/O 密集型**: 数据库、网络调用等异步操作

### Q2: 响应式编程的缺点是什么？

**答案**:
- **学习曲线陡峭**: 需要理解响应式编程范式
- **调试困难**: 异步调用链难以追踪
- **生态不完善**: 部分库不支持响应式
- **CPU 密集型不适用**: 计算密集型任务不适合

### Q3: 如何调试响应式代码？

**答案**:
1. 使用 `.log()` 操作符打印日志
2. 使用 `.checkpoint()` 标记调用点
3. 启用 Reactor 调试模式: `Hooks.onOperatorDebug()`
4. 使用 BlockHound 检测阻塞调用

### Q4: flatMap 和 map 有什么区别？

**答案**:
- **map**: 一对一转换，返回普通对象
  ```java
  Flux.just(1, 2, 3).map(i -> i * 2)  // Flux<Integer>
  ```
- **flatMap**: 一对多转换，返回 Publisher，会自动展平
  ```java
  Flux.just(1, 2, 3).flatMap(i -> Flux.just(i, i * 2))  // Flux<Integer>
  ```

### Q5: 如何处理阻塞调用？

**答案**:
1. 使用 `subscribeOn(Schedulers.boundedElastic())` 隔离到专用线程池
2. 使用 `Mono.fromCallable()` 包装阻塞代码
3. 设置超时: `.timeout(Duration.ofSeconds(30))`
4. 示例:
   ```java
   Mono.fromCallable(() -> blockingOperation())
       .subscribeOn(Schedulers.boundedElastic())
       .timeout(Duration.ofSeconds(30));
   ```

### Q6: 如何实现分页查询？

**答案**:
```java
@GetMapping
public Flux<User> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return userRepository.findAll()
        .skip((long) page * size)
        .take(size);
}
```

### Q7: 如何处理循环依赖？

**答案**:
- 使用构造器注入
- 使用 `@Lazy` 注解
- 重构代码，消除循环依赖

### Q8: WebFlux 和 MVC 可以混用吗？

**答案**:
- 不建议在同一个应用中混用
- 可以通过网关分离不同服务
- 如果必须混用，确保理解线程模型差异

### Q9: 如何测试响应式代码？

**答案**:
使用 `StepVerifier`:
```java
@Test
void testGetAllUsers() {
    Flux<User> users = userService.getAllUsers();
    
    StepVerifier.create(users)
        .expectNextCount(3)
        .verifyComplete();
}
```

### Q10: 生产环境部署注意事项？

**答案**:
1. **配置优化**:
   - 调整 Netty 线程数
   - 配置数据库连接池
   - 设置合理的超时时间

2. **监控**:
   - 集成 Prometheus + Grafana
   - 启用 Spring Boot Actuator
   - 配置链路追踪

3. **安全**:
   - 添加认证授权
   - 限流防护
   - 关闭不必要的端点

4. **日志**:
   - 使用异步日志
   - 配置日志级别
   - 集中式日志收集

---

## 项目亮点总结

### 技术亮点
1. ✅ **完全响应式**: Web/Service/Repository 全链路非阻塞
2. ✅ **事件驱动**: Kafka 实现异步解耦
3. ✅ **多数据源**: R2DBC + MongoDB 混合使用
4. ✅ **缓存优化**: Redis 提升查询性能
5. ✅ **异步通知**: 邮件异步发送，不阻塞主流程
6. ✅ **生产就绪**: 全局异常、监控、文档齐全

### 架构亮点
1. ✅ **分层清晰**: Controller/Service/Repository 职责明确
2. ✅ **配置灵活**: 通过开关控制功能启用
3. ✅ **容错设计**: 降级、重试、超时机制
4. ✅ **可扩展性**: 易于添加新功能
5. ✅ **可维护性**: 代码规范，注释完善

---

## 总结

这个项目展示了现代化 Java 后端开发的最佳实践：
- 使用响应式编程提高并发性能
- 使用事件驱动架构实现解耦
- 使用缓存提升查询效率
- 使用监控保障系统稳定性

**面试时的核心策略**:
1. 强调**响应式编程的优势**和适用场景
2. 展示对**核心组件实现原理**的深入理解
3. 能够快速定位代码位置并**解释实现逻辑**
4. 准备好讨论**性能优化**和**生产环境**经验
5. 展示**问题排查**和**系统设计**能力

**快速复习清单**:
- [ ] Mono/Flux 基本操作符
- [ ] flatMap/map/filter 等转换操作
- [ ] onErrorResume/retry 等错误处理
- [ ] subscribeOn/publishOn 线程调度
- [ ] Kafka 生产消费原理
- [ ] Redis 缓存策略
- [ ] R2DBC 响应式数据库访问
- [ ] 全局异常处理机制
- [ ] 项目核心文件位置

祝面试顺利！🎉
