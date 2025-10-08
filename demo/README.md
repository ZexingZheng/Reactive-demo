# Reactive CRUD 项目

基于 Spring WebFlux 和 Reactor 的响应式 CRUD 应用。

## 技术栈

- Spring Boot 3.2.0
- Spring WebFlux
- Spring Data R2DBC
- H2 内存数据库 (R2DBC驱动)
- Lombok
- Java 17

## 项目结构

```
src/main/java/com/example/reactive/
├── entity/
│   └── User.java              # 用户实体类
├── repository/
│   └── UserRepository.java    # 数据访问层
├── service/
│   └── UserService.java       # 业务逻辑层
├── controller/
│   └── UserController.java    # REST API控制器
└── ReactiveCrudApplication.java  # 主启动类
```

## API 接口

### 查询所有用户
```
GET http://localhost:8080/api/users
```

### 根据ID查询用户
```
GET http://localhost:8080/api/users/{id}
```

### 根据名称搜索用户
```
GET http://localhost:8080/api/users/search?name=张三
```

### 创建用户
```
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "name": "赵六",
  "email": "zhaoliu@example.com",
  "age": 35
}
```

### 更新用户
```
PUT http://localhost:8080/api/users/{id}
Content-Type: application/json

{
  "name": "赵六",
  "email": "zhaoliu@example.com",
  "age": 36
}
```

### 删除用户
```
DELETE http://localhost:8080/api/users/{id}
```

## 启动项目

```bash
mvn spring-boot:run
```

## 测试示例

```bash
# 查询所有用户
curl http://localhost:8080/api/users

# 查询指定用户
curl http://localhost:8080/api/users/1

# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"赵六","email":"zhaoliu@example.com","age":35}'

# 更新用户
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com","age":26}'

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1
```

## 后续扩展

- 集成 MongoDB (Reactive)
- 集成 Kafka
- 添加异常处理
- 添加单元测试
