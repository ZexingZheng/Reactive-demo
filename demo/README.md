# Reactive CRUD 项目

基于 Spring WebFlux 和 Reactor 的现代化响应式 CRUD 应用，集成了当前主流的 Java 开发技术栈。

## 🚀 技术栈

### 核心框架
- **Spring Boot 3.2.0** - 现代化 Java 应用框架
- **Spring WebFlux** - 响应式 Web 框架（基于 Netty）
- **Project Reactor** - 响应式编程库

### 数据存储
- **Spring Data R2DBC** - 响应式关系型数据库访问
- **H2 Database** - 内存数据库（开发/测试用）
- **Spring Data MongoDB Reactive** - MongoDB 响应式驱动
- **Spring Data Redis Reactive** - Redis 响应式缓存

### 消息队列
- **Apache Kafka** - 分布式消息队列
- **Reactor Kafka** - Kafka 响应式客户端

### API 文档
- **SpringDoc OpenAPI 3** - Swagger UI / OpenAPI 文档

### 监控运维
- **Spring Boot Actuator** - 应用监控端点
- **Micrometer + Prometheus** - 指标监控

### 其他
- **Lombok** - 简化 Java 代码
- **Jakarta Validation** - 参数校验
- **Spring Mail** - 邮件发送

## 📁 项目结构

```
src/main/java/com/example/reactive/
├── config/                     # 配置类
│   ├── EmailConfig.java        # 邮件配置
│   ├── KafkaConfig.java        # Kafka 配置
│   ├── RedisConfig.java        # Redis 缓存配置
│   └── WebConfig.java          # Web CORS 配置
├── controller/                 # REST API 控制器
│   ├── UserController.java    # R2DBC 用户接口
│   └── UserMongoController.java # MongoDB 用户接口
├── entity/                     # 实体类
│   ├── User.java              # R2DBC 用户实体
│   └── UserMongo.java         # MongoDB 用户实体
├── repository/                # 数据访问层
│   ├── UserRepository.java    # R2DBC 仓库
│   └── UserMongoRepository.java # MongoDB 仓库
├── service/                   # 业务逻辑层
│   ├── UserService.java       # R2DBC 用户服务
│   ├── UserMongoService.java  # MongoDB 用户服务
│   └── EmailService.java      # 邮件服务
├── kafka/                     # Kafka 相关
│   ├── ReactiveKafkaProducer.java  # 消息生产者
│   └── ReactiveKafkaConsumer.java  # 消息消费者
├── event/                     # 事件定义
│   └── UserEvent.java         # 用户事件
├── exception/                 # 异常处理
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   └── ResourceNotFoundException.java # 资源不存在异常
└── ReactiveCrudApplication.java # 主启动类

src/main/resources/
├── static/                    # 静态资源
│   └── index.html            # 测试前端页面
├── application.yml           # 应用配置
├── schema.sql               # 数据库结构
└── data.sql                 # 初始化数据
```

## 🎯 核心功能

### 1. 响应式 CRUD 操作
- ✅ 完全非阻塞的数据库操作（R2DBC）
- ✅ MongoDB 支持（可选）
- ✅ Redis 缓存支持（可选）
- ✅ 全局异常处理
- ✅ 参数校验

### 2. Kafka 事件驱动
- ✅ 异步消息发送
- ✅ 响应式消息消费
- ✅ 用户 CRUD 事件通知

### 3. API 文档
- ✅ Swagger UI 交互式文档
- ✅ OpenAPI 3.0 规范

### 4. 监控运维
- ✅ Health 健康检查
- ✅ Metrics 指标收集
- ✅ Prometheus 端点

### 5. 前端测试页面
- ✅ 美观的 Web 界面
- ✅ 完整的 CRUD 操作
- ✅ 实时响应展示

## 🔧 快速开始

### 前置要求
- ☕ Java 17+
- 🔧 Maven 3.6+
- 🐳 Docker & Docker Compose（可选）

### 方式一：仅使用 H2 内存数据库（最简单）

```bash
# 1. 克隆项目
git clone <repository-url>
cd demo

# 2. 编译运行
mvn clean package
mvn spring-boot:run

# 3. 访问应用
# - 前端测试页面: http://localhost:9090
# - Swagger UI: http://localhost:9090/swagger-ui.html
# - Actuator: http://localhost:9090/actuator
```

### 方式二：使用完整技术栈（推荐）

```bash
# 1. 启动依赖服务（MongoDB, Redis, Kafka）
cd demo
docker-compose up -d

# 2. 修改配置启用 MongoDB 和 Redis
# 编辑 src/main/resources/application.yml:
mongodb:
  enabled: true
redis:
  enabled: true

# 3. 编译运行
mvn clean package
mvn spring-boot:run

# 4. 访问应用
# - 前端测试页面: http://localhost:9090
# - MongoDB API: http://localhost:9090/api/mongo/users
# - R2DBC API: http://localhost:9090/api/users
# - Swagger UI: http://localhost:9090/swagger-ui.html
```

## 📡 API 接口

### R2DBC 用户接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/users` | 获取所有用户 |
| GET | `/api/users/{id}` | 根据ID获取用户 |
| GET | `/api/users/search?name={name}` | 按姓名搜索 |
| POST | `/api/users` | 创建用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |

### MongoDB 用户接口（启用时）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/mongo/users` | 获取所有用户 |
| GET | `/api/mongo/users/{id}` | 根据ID获取用户 |
| GET | `/api/mongo/users/search?name={name}` | 按姓名模糊搜索 |
| POST | `/api/mongo/users` | 创建用户 |
| PUT | `/api/mongo/users/{id}` | 更新用户 |
| DELETE | `/api/mongo/users/{id}` | 删除用户 |

### 测试示例

```bash
# 创建用户
curl -X POST http://localhost:9090/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com","age":25}'

# 查询所有用户
curl http://localhost:9090/api/users

# 查询指定用户
curl http://localhost:9090/api/users/1

# 更新用户
curl -X PUT http://localhost:9090/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com","age":26}'

# 删除用户
curl -X DELETE http://localhost:9090/api/users/1
```

## 🎨 前端测试页面

访问 `http://localhost:9090` 可以看到一个美观的 Web 界面，提供：

- ✅ 用户创建表单
- ✅ 用户查询（全部/ID/姓名）
- ✅ 用户更新表单
- ✅ 用户删除功能
- ✅ 实时响应结果展示
- ✅ 用户列表表格
- ✅ 加载状态指示

## 📊 监控端点

访问 `http://localhost:9090/actuator` 查看所有可用端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 指标数据
- `/actuator/prometheus` - Prometheus 格式指标
- `/actuator/env` - 环境变量
- `/actuator/beans` - Spring Beans
- `/actuator/mappings` - 请求映射

## 📖 配置说明

### application.yml 主要配置

```yaml
# MongoDB 开关
mongodb:
  enabled: false  # 设置为 true 启用

# Redis 开关
redis:
  enabled: false  # 设置为 true 启用

# 邮件开关
email:
  enabled: true   # 设置为 false 禁用

# 服务端口
server:
  port: 9090
```

## 🧪 性能特点

### 响应式优势
- **高并发处理** - 基于事件驱动，少量线程处理大量请求
- **非阻塞 I/O** - 数据库、缓存、消息队列全部非阻塞
- **背压支持** - 自动处理生产者-消费者速率不匹配
- **资源高效** - 内存占用低，吞吐量高

### 对比传统阻塞模式

| 特性 | 传统模式 | 响应式模式 |
|------|---------|-----------|
| 线程模型 | 每请求一线程 | 事件驱动 |
| 并发能力 | 受限于线程池 | 可处理数万并发 |
| 资源占用 | 高（线程开销大） | 低（少量线程） |
| I/O 操作 | 阻塞 | 非阻塞 |
| 延迟 | 较高 | 较低 |

## 🔒 安全特性

- ✅ 全局异常处理
- ✅ 参数校验
- ✅ CORS 配置
- ✅ 错误信息统一格式

## 🚀 进阶扩展

### 已实现
- [x] Spring WebFlux 响应式 Web
- [x] R2DBC 响应式数据库
- [x] MongoDB 集成
- [x] Redis 缓存
- [x] Kafka 消息队列
- [x] Swagger API 文档
- [x] Actuator 监控
- [x] 全局异常处理
- [x] 前端测试页面
- [x] Docker Compose 部署

### 可选扩展
- [ ] Spring Security 安全认证
- [ ] JWT Token 认证
- [ ] Spring Cloud Gateway 网关
- [ ] Spring Cloud Sleuth 链路追踪
- [ ] Kubernetes 部署
- [ ] CI/CD 流水线
- [ ] 单元测试覆盖
- [ ] 集成测试

## 📚 相关文档

- [QUICK-START.md](QUICK-START.md) - 快速启动指南
- [README-KAFKA.md](README-KAFKA.md) - Kafka 详解
- [README-EMAIL.md](README-EMAIL.md) - 邮件功能详解
- [ARCHITECTURE.md](ARCHITECTURE.md) - 架构设计详解

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

**⭐ 如果这个项目对你有帮助，请给一个 Star！**
