# Reactive CRUD with Kafka Reactor + WebFlux

## 架构说明

这是一个完全**异步非阻塞**的响应式应用,集成了:

- **Spring WebFlux**: 响应式 Web 框架
- **R2DBC**: 响应式数据库访问
- **Reactor Kafka**: 响应式 Kafka 客户端

## 异步非阻塞流程

### 1. HTTP 请求处理 (完全非阻塞)
```
Client -> Controller (返回 Mono/Flux)
       -> Service (R2DBC 异步操作)
       -> Kafka Producer (异步发送消息)
       -> 立即返回给 Client (不等待 Kafka)
```

### 2. Kafka 消息处理 (完全非阻塞)
```
Kafka Topic -> Reactive Consumer (响应式订阅)
            -> 异步处理业务逻辑
            -> 手动提交 offset
```

## 关键特性

### ✅ 完全异步非阻塞
- **HTTP 层**: WebFlux 基于 Netty,全程事件驱动
- **数据库层**: R2DBC 使用非阻塞 I/O
- **消息层**: Reactor Kafka 使用响应式流

### ✅ 背压支持
- Reactor 自动处理背压
- Kafka Consumer 可控制消费速率

### ✅ 弹性处理
- Kafka 发送失败不影响主流程
- Consumer 自动重试机制

## 启动说明

### 1. 启动 Kafka
```bash
# 使用 Docker 快速启动
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

### 2. 创建 Topic (可选,会自动创建)
```bash
docker exec -it kafka kafka-topics.sh \
  --create \
  --topic user-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 3. 启动应用
```bash
mvn clean package
mvn spring-boot:run
```

## 测试 API

### 创建用户 (异步发送 Kafka 消息)
```bash
curl -X POST http://localhost:9090/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com","age":25}'
```

### 查看日志
应用启动后会自动消费 Kafka 消息,查看控制台日志:
```
Received user event: type=CREATE, userId=1, timestamp=2025-10-07T...
Processing CREATE event for user: 张三
```

## 性能特点

### 传统阻塞模式
- **每个请求一个线程**
- 数据库操作阻塞线程
- Kafka 发送阻塞线程
- 并发能力受限于线程池

### Reactor 模式 (当前实现)
- **少量线程处理海量并发**
- 数据库操作不阻塞线程
- Kafka 操作不阻塞线程
- 事件驱动,资源利用率高

## 监控 Kafka 消息

```bash
# 查看消息
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## 扩展建议

1. **添加消息重试队列**: 处理失败的消息
2. **添加 Dead Letter Queue**: 处理无法处理的消息
3. **添加监控**: Micrometer + Prometheus
4. **添加链路追踪**: Spring Cloud Sleuth
5. **水平扩展**: 增加 Kafka 分区和 Consumer 实例
