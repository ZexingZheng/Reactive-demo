# 快速启动指南

## 🚀 5分钟快速体验

### 前置要求
- ☕ Java 17+
- 🐘 Maven 3.6+
- 🐳 Docker (运行 Kafka)

---

## 步骤 1: 启动 Kafka

```bash
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
```

等待 10 秒让 Kafka 启动完成。

---

## 步骤 2: 配置邮件 (可选)

### 选项 A: 跳过邮件功能 (测试)
```yaml
# application.yml 保持默认
email:
  enabled: false  # 已经是 false
```

### 选项 B: 使用 Gmail
编辑 `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # 从 Google 获取应用专用密码

email:
  enabled: true  # 改为 true
```

> 📖 **如何获取 Gmail 应用密码**: 见 `README-EMAIL.md`

---

## 步骤 3: 启动应用

```bash
# 编译
mvn clean package

# 运行
mvn spring-boot:run
```

等待看到:
```
Started ReactiveCrudApplication in 3.5 seconds
Started Kafka reactive consumer for topic: user-events
```

---

## 步骤 4: 测试 API

### 1. 创建用户
```bash
curl -X POST http://localhost:9090/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "email": "zhangsan@example.com",
    "age": 25
  }'
```

**期望响应** (< 100ms):
```json
{
  "id": 1,
  "name": "张三",
  "email": "zhangsan@example.com",
  "age": 25
}
```

**控制台日志** (后台异步执行):
```
Received user event: type=CREATE, userId=1
Processing CREATE event for user: 张三
Preparing to send email to: zhangsan@example.com
Successfully sent email to: zhangsan@example.com
```

---

### 2. 查询所有用户
```bash
curl http://localhost:9090/api/users
```

---

### 3. 查询单个用户
```bash
curl http://localhost:9090/api/users/1
```

---

### 4. 更新用户
```bash
curl -X PUT http://localhost:9090/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三-更新",
    "email": "zhangsan-updated@example.com",
    "age": 26
  }'
```

---

### 5. 删除用户
```bash
curl -X DELETE http://localhost:9090/api/users/1
```

---

## 步骤 5: 验证异步流程

### 监控 Kafka 消息
```bash
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

**你会看到**:
```json
{"eventType":"CREATE","userId":1,"userName":"张三","userEmail":"zhangsan@example.com","userAge":25,"timestamp":"2025-10-07T10:30:00"}
{"eventType":"UPDATE","userId":1,"userName":"张三-更新","userEmail":"zhangsan-updated@example.com","userAge":26,"timestamp":"2025-10-07T10:35:00"}
{"eventType":"DELETE","userId":1,"userName":null,"userEmail":null,"userAge":null,"timestamp":"2025-10-07T10:40:00"}
```

---

## 步骤 6: 压力测试 (可选)

### 安装 wrk
```bash
# macOS
brew install wrk

# Ubuntu
sudo apt-get install wrk
```

### 测试创建用户
```bash
# 创建测试文件
cat > create-user.lua << 'EOF'
wrk.method = "POST"
wrk.body   = '{"name":"Test User","email":"test@example.com","age":25}'
wrk.headers["Content-Type"] = "application/json"
EOF

# 运行压测: 4线程, 100并发, 持续10秒
wrk -t4 -c100 -d10s -s create-user.lua http://localhost:9090/api/users
```

**期望结果**:
```
Requests/sec:   3000+
Latency:        30-50ms
```

---

## 常见问题

### Q1: Kafka 启动失败
**检查端口占用**:
```bash
# Windows
netstat -ano | findstr 9092

# Mac/Linux
lsof -i :9092
```

**解决**: 停止占用端口的进程或更换端口

---

### Q2: 应用启动报错 "Connection refused"
**原因**: Kafka 还没启动完成

**解决**:
```bash
# 检查 Kafka 状态
docker logs kafka

# 等待看到 "started (kafka.server.KafkaServer)"
```

---

### Q3: 邮件发送失败
**检查配置**:
1. 确认邮箱密码是**应用专用密码**,不是登录密码
2. 确认开启了 SMTP 服务
3. 查看详细日志:
```yaml
logging:
  level:
    org.springframework.mail: DEBUG
```

**临时解决**:
```yaml
email:
  enabled: false  # 关闭邮件功能
```

---

### Q4: 控制台没有日志
**增加日志级别**:
```yaml
logging:
  level:
    com.example.reactive: DEBUG
    reactor.kafka: DEBUG
```

---

## 清理环境

### 停止应用
```bash
Ctrl+C
```

### 停止并删除 Kafka
```bash
docker stop kafka
docker rm kafka
```

---

## 下一步

### 📚 深入学习
- [README-KAFKA.md](README-KAFKA.md) - Kafka Reactor 详解
- [README-EMAIL.md](README-EMAIL.md) - 邮件功能详解
- [ARCHITECTURE.md](ARCHITECTURE.md) - 架构设计详解

### 🛠 进阶实践
1. **添加 Redis 缓存**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
   </dependency>
   ```

2. **添加 API 文档**
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
   </dependency>
   ```

3. **添加监控**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

4. **部署到 K8s**
   ```bash
   # 构建镜像
   mvn spring-boot:build-image

   # 部署
   kubectl apply -f k8s/deployment.yaml
   ```

---

## 🎉 恭喜!

你已经成功运行了一个:
- ✅ 完全异步非阻塞的响应式应用
- ✅ 使用 Kafka 进行事件驱动
- ✅ 集成了异步邮件发送
- ✅ 支持高并发场景

享受响应式编程的魅力吧! 🚀
