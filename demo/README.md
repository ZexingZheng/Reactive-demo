# Reactive CRUD Project

## 1. Technology Stack
* **Core Framework**: Spring Boot 3.2.0, Spring WebFlux, Project Reactor
* **Data Storage**: Spring Data R2DBC (H2/MySQL), MongoDB Reactive, Redis Reactive
* **Messaging**: Apache Kafka (Reactor Kafka)
* **API & Ops**: OpenAPI 3 (Swagger), Spring Boot Actuator, Micrometer
* **Others**: Lombok, Jakarta Validation, Java 17

## 2. Project Structure
* `config/`: Configuration for Email, Kafka, Redis, and CORS.
* `controller/`: REST controllers for both R2DBC and MongoDB implementations.
* `entity/`: Data models for R2DBC and MongoDB.
* `repository/`: Reactive repository interfaces.
* `service/`: Business logic handling Mono and Flux streams.
* `kafka/`: Reactive producer and consumer implementations.
* `exception/`: Global exception handling and custom error responses.
* `resources/`: Application properties, SQL scripts, and static test pages.

## 3. Core Features
1. **Reactive CRUD**: Fully non-blocking database operations for User management.
2. **Event-Driven**: Asynchronous user event processing via Kafka.
3. **API Documentation**: Interactive Swagger UI for testing endpoints.
4. **Monitoring**: Health checks and performance metrics via Actuator.

## 4. Getting Started
### Prerequisites
* Java 17+
* Maven 3.6+
* Docker (Optional, for full stack services)

### Steps
1. Clone the repository.
2. Run `mvn clean package`.
3. Start the app: `mvn spring-boot:run`.
4. Access the UI: `http://localhost:9090`.

## 5. Main API Endpoints
* `GET /api/users`: Retrieve all users.
* `GET /api/users/{id}`: Find user by ID.
* `POST /api/users`: Create a new user.
* `PUT /api/users/{id}`: Update existing user.
* `DELETE /api/users/{id}`: Remove a user.

## 6. Reactive vs Traditional Comparison
* **Threading**: Traditional uses one-thread-per-request; Reactive uses event-loop with minimal threads.
* **Concurrency**: Reactive handles high concurrent loads more efficiently with lower memory overhead.
* **I/O**: Traditional I/O is blocking; Reactive I/O is non-blocking with backpressure support.

## 7. Future Enhancements
* Security integration with Spring Security & JWT.
* Implementation of Spring Cloud Gateway.
* Distributed tracing with Micrometer Tracing.
