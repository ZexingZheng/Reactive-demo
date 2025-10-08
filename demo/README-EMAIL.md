# 异步非阻塞邮件发送功能

## 架构说明

邮件功能已完全集成到响应式流程中,特点:

### ✅ 完全异步非阻塞
- 使用**专用线程池**处理阻塞的 JavaMailSender
- 通过 `Mono.subscribeOn(emailScheduler)` 将邮件发送隔离
- 不阻塞主业务流程和 Kafka 消费线程

### ✅ 事件驱动
```
用户操作 → Kafka 消息 → Consumer 异步消费 → 异步发送邮件
```

## 异步流程详解

### 1. 创建用户流程
```
POST /api/users
    ↓
UserService.createUser()
    ↓
保存到数据库 (R2DBC 异步)
    ↓
发送 Kafka 消息 (Reactor Kafka 异步)
    ↓
立即返回结果给客户端 ✓
    ↓
--- 以下在后台异步执行 ---
    ↓
Kafka Consumer 接收消息
    ↓
EmailService.sendEmail() (在专用线程池执行)
    ↓
发送欢迎邮件
```

### 2. 关键技术点

#### a) 专用线程池 (EmailConfig.java:16-24)
```java
@Bean(name = "emailScheduler")
public Scheduler emailScheduler() {
    return Schedulers.fromExecutor(
        Executors.newFixedThreadPool(10)  // 10个线程处理邮件发送
    );
}
```

#### b) 异步包装 (EmailService.java:42-62)
```java
public Mono<Void> sendEmail(EmailMessage emailMessage) {
    return Mono.fromCallable(() -> {
        sendEmailSync(emailMessage);  // 阻塞操作
        return true;
    })
    .subscribeOn(emailScheduler)  // 在专用线程池执行
    .timeout(Duration.ofSeconds(30))
    .onErrorResume(e -> Mono.empty());  // 失败不影响主流程
}
```

#### c) Kafka Consumer 集成 (ReactiveKafkaConsumer.java:73-98)
```java
private Mono<Void> processEvent(UserEvent event) {
    return switch (event.getEventType()) {
        case "CREATE" -> {
            EmailMessage email = EmailMessage.createUserWelcomeEmail(...);
            yield emailService.sendEmail(email);  // 异步发送
        }
        // ...
    };
}
```

## 配置说明

### 1. Gmail 配置 (推荐测试)

#### 步骤 1: 开启两步验证
1. 访问 https://myaccount.google.com/security
2. 开启"两步验证"

#### 步骤 2: 生成应用专用密码
1. 访问 https://myaccount.google.com/apppasswords
2. 选择"邮件"和"其他设备"
3. 生成16位密码
   blux nkxn vhwm hlrd
#### 步骤 3: 配置 application.yml
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: blux nkxn vhwm hlrd  # 刚才生成的密码

email:
  enabled: true  # 开启邮件功能
```

### 2. 其他邮件服务器

#### QQ邮箱
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-qq@qq.com
    password: your-authorization-code
```

#### 163邮箱
```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your-email@163.com
    password: your-authorization-code
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

#### Outlook
```yaml
spring:
  mail:
    host: smtp-mail.outlook.com
    port: 587
    username: your-email@outlook.com
    password: your-password
```

## 测试邮件功能

### 1. 启动服务
```bash
# 启动 Kafka
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ENABLE_KRAFT=yes \
  -e KAFKA_CFG_PROCESS_ROLES=broker,controller \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e ALLOW_PLAINTEXT_LISTENER=yes \
  bitnami/kafka:latest

# 启动应用
mvn spring-boot:run
```

### 2. 创建用户 (触发欢迎邮件)
```bash
curl -X POST http://localhost:9090/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "email": "zhangsan@example.com",
    "age": 25
  }'
```

### 3. 查看日志
```
2025-10-07 ... Received user event: type=CREATE, userId=1
2025-10-07 ... Processing CREATE event for user: 张三
2025-10-07 ... Preparing to send email to: zhangsan@example.com
2025-10-07 ... Successfully sent email to: zhangsan@example.com
```

### 4. 检查邮箱
用户 `zhangsan@example.com` 会收到欢迎邮件 📧

## 邮件模板

已内置三种邮件模板 (EmailMessage.java):

### 1. 欢迎邮件 (CREATE 事件)
- **触发**: 创建用户
- **内容**: 欢迎加入
- **样式**: HTML 格式,带边框和样式

### 2. 更新通知 (UPDATE 事件)
- **触发**: 更新用户信息
- **内容**: 信息已更新提醒
- **样式**: HTML 格式

### 3. 删除通知 (DELETE 事件)
- **触发**: 删除用户
- **注意**: 需要在删除前获取邮箱地址

## 性能特点

### 传统阻塞方式
```java
// ❌ 阻塞主线程
user = userRepository.save(user);  // 阻塞
kafkaProducer.send(message);       // 阻塞
emailService.send(email);          // 阻塞 2-5 秒!
return user;                       // 用户等待 3-6 秒
```

### 当前异步方式
```java
// ✅ 完全异步
user = userRepository.save(user)   // 异步 R2DBC
    .flatMap(u -> kafkaProducer.send())  // 异步 Kafka
    .thenReturn(user);             // 立即返回给用户 < 100ms

// 邮件在后台发送,不阻塞
```

## 监控和调试

### 1. 查看邮件线程池状态
```bash
# 添加 actuator 依赖后
curl http://localhost:9090/actuator/metrics/executor.active
```

### 2. 开启邮件调试日志
```yaml
logging:
  level:
    org.springframework.mail: DEBUG
    com.sun.mail: TRACE
```

### 3. 测试模式 (不真实发送)
```yaml
email:
  enabled: false  # 关闭邮件发送,仅打印日志
```

## 扩展建议

### 1. 添加邮件模板引擎
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2. 添加邮件队列和重试
```java
public Mono<Void> sendEmailWithRetry(EmailMessage message) {
    return emailService.sendEmail(message)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
}
```

### 3. 添加邮件发送记录表
```java
@Table("email_logs")
public class EmailLog {
    private Long id;
    private String to;
    private String subject;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```

### 4. 添加附件支持
```java
public Mono<Void> sendEmailWithAttachment(
    EmailMessage message,
    List<File> attachments) {
    // ...
}
```

## 故障排查

### 问题 1: 邮件发送超时
**解决**: 增加超时时间
```yaml
spring:
  mail:
    properties:
      mail:
        smtp:
          timeout: 10000
```

### 问题 2: 认证失败
**解决**:
- 确认使用应用专用密码 (不是登录密码)
- 检查邮箱是否开启 SMTP 服务

### 问题 3: 邮件进入垃圾箱
**解决**:
- 配置 SPF 记录
- 使用企业邮箱
- 添加发件人签名

## 安全建议

### 1. 不要硬编码密码
```bash
# 使用环境变量
export MAIL_PASSWORD=your-password
```

```yaml
spring:
  mail:
    password: ${MAIL_PASSWORD}
```

### 2. 限流保护
```java
@Service
public class EmailService {
    private final RateLimiter rateLimiter =
        RateLimiter.create(10.0); // 每秒最多10封

    public Mono<Void> sendEmail(EmailMessage message) {
        if (!rateLimiter.tryAcquire()) {
            return Mono.error(new TooManyRequestsException());
        }
        // ...
    }
}
```

## 总结

✅ **完全异步**: 邮件发送不阻塞主流程
✅ **解耦**: 通过 Kafka 解耦业务和邮件
✅ **弹性**: 邮件失败不影响业务成功
✅ **高性能**: 专用线程池,资源隔离
✅ **可扩展**: 支持多种邮件服务器和模板
