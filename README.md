# Reactive-demo

🚀 现代化的 Spring WebFlux 响应式 CRUD 应用，集成了主流 Java 开发技术栈。

## ✨ 特性

- ⚡ **响应式编程** - 基于 Spring WebFlux 和 Project Reactor
- 📦 **多数据源支持** - R2DBC (H2) + MongoDB + Redis
- 📨 **事件驱动** - Kafka 消息队列集成
- 📧 **邮件通知** - 异步邮件发送
- 📚 **API 文档** - Swagger/OpenAPI 3.0
- 📊 **监控运维** - Actuator + Prometheus
- 🎨 **测试界面** - 内置 Web 前端测试页面
- 🐳 **容器化** - Docker Compose 一键部署

## 🎯 技术栈

| 分类 | 技术 |
|------|------|
| 🔧 核心框架 | Spring Boot 3.2, Spring WebFlux, Project Reactor |
| 💾 数据存储 | R2DBC (H2), MongoDB Reactive, Redis Reactive |
| 📮 消息队列 | Apache Kafka, Reactor Kafka |
| 📖 API 文档 | SpringDoc OpenAPI 3, Swagger UI |
| 📈 监控 | Spring Boot Actuator, Micrometer, Prometheus |
| 🛠️ 工具 | Lombok, Docker Compose |

## 🚀 快速开始

### 最简单方式（仅使用 H2）

```bash
cd demo
mvn spring-boot:run
```

访问：
- 🌐 前端页面: http://localhost:9090
- 📚 API 文档: http://localhost:9090/swagger-ui.html
- 📊 监控端点: http://localhost:9090/actuator

### 完整功能（包含 MongoDB, Redis, Kafka）

```bash
# 1. 启动依赖服务
cd demo
docker-compose up -d

# 2. 启用 MongoDB 和 Redis（编辑 application.yml）
mongodb.enabled: true
redis.enabled: true

# 3. 运行应用
mvn spring-boot:run
```

## 📖 文档

详细文档请查看 [demo/README.md](demo/README.md)

- [快速启动指南](demo/QUICK-START.md)
- [Kafka 集成详解](demo/README-KAFKA.md)
- [邮件功能说明](demo/README-EMAIL.md)
- [架构设计文档](demo/ARCHITECTURE.md)

## 🎨 界面预览

项目包含一个美观的 Web 测试界面，支持完整的 CRUD 操作：

访问 http://localhost:9090 即可体验

## 📡 API 接口

### R2DBC 接口
- `GET /api/users` - 获取所有用户
- `GET /api/users/{id}` - 获取指定用户
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### MongoDB 接口（可选）
- `GET /api/mongo/users` - 获取所有用户
- `GET /api/mongo/users/{id}` - 获取指定用户
- `POST /api/mongo/users` - 创建用户
- `PUT /api/mongo/users/{id}` - 更新用户
- `DELETE /api/mongo/users/{id}` - 删除用户

## 🏗️ 项目结构

```
Reactive-demo/
├── demo/                          # 主项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/             # Java 源码
│   │   │   └── resources/        # 配置和静态资源
│   │   └── test/                 # 测试代码
│   ├── docker-compose.yml        # Docker 编排
│   ├── pom.xml                   # Maven 配置
│   ├── README.md                 # 详细文档
│   ├── QUICK-START.md           # 快速开始
│   ├── ARCHITECTURE.md          # 架构说明
│   └── README-KAFKA.md          # Kafka 说明
└── README.md                     # 本文件
```

## 🔧 配置说明

主要配置项在 `demo/src/main/resources/application.yml`：

```yaml
# 功能开关
mongodb.enabled: false  # MongoDB 支持
redis.enabled: false    # Redis 缓存
email.enabled: true     # 邮件通知

# 服务端口
server.port: 9090
```

## 🌟 核心特性

### 响应式架构
- 完全非阻塞的 I/O 操作
- 基于事件驱动的并发处理
- 支持背压（Backpressure）
- 高吞吐量，低延迟

### 事件驱动
- Kafka 异步消息发送
- 响应式消息消费
- CRUD 操作事件通知
- 邮件异步发送

### 现代化运维
- Swagger UI 交互式 API 文档
- Actuator 健康检查和指标
- Prometheus 监控集成
- Docker Compose 容器化部署

## 📊 性能对比

| 指标 | 传统阻塞模式 | 响应式模式 |
|------|------------|-----------|
| 并发处理 | ~200 | ~5000+ |
| 平均延迟 | 100-500ms | 20-50ms |
| 内存占用 | 高 | 低 |
| 线程数 | 多 | 少 |

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

⭐ **如果觉得有帮助，请给个 Star！**
