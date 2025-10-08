# Quick Start Guide

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- (Optional) curl and jq for testing

## Setup

### 1. Configure Email Settings
Before running the application, configure your email settings in `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-password  # Or use app-specific password
```

**Note**: For Gmail, you may need to:
- Enable "Less secure app access" or
- Use an "App Password" if you have 2FA enabled

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run Tests
```bash
mvn test
```

### 4. Start the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing the API

### Using the Demo Script
We provide a demo script that exercises all CRUD operations:

```bash
./demo-api-usage.sh
```

This will:
- Create users (triggers welcome emails)
- Update users (triggers update notifications)
- Delete users (triggers goodbye emails)
- Demonstrate batching with multiple concurrent requests

### Manual Testing

#### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john.doe@example.com"}'
```

Expected response:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

A welcome email will be sent asynchronously to `john.doe@example.com`.

#### Get All Users
```bash
curl http://localhost:8080/api/users
```

#### Get Specific User
```bash
curl http://localhost:8080/api/users/1
```

#### Update a User
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"John Updated","email":"john.updated@example.com"}'
```

An update notification email will be sent.

#### Delete a User
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

A goodbye email will be sent before deletion.

## Observing Email Optimizations

### Check Application Logs
When running the application, you'll see logs showing:

```
INFO  c.r.d.s.EmailService - Email sent successfully to: john.doe@example.com
INFO  c.r.d.s.UserService - User created with email notification: john.doe@example.com
INFO  c.r.d.s.EmailService - Sending batch of 5 emails
INFO  c.r.d.s.EmailService - Bulk email sending completed for 10 emails
```

### Performance Testing

#### Test Single Email Performance
```bash
time curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com"}'
```

Notice: The response is immediate (< 100ms) because email is sent asynchronously.

#### Test Bulk Email Performance
Create a script to create 100 users:

```bash
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"User $i\",\"email\":\"user$i@example.com\"}" &
done
wait
```

Observe in logs:
- Emails are sent in batches
- Controlled concurrency (5 parallel sends)
- All 100 emails sent in ~5 seconds

## Development Mode

### Using H2 Console (Optional)
To enable H2 console for database inspection, add to `application.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

### Hot Reload
For development, use Spring Boot DevTools:

```bash
mvn spring-boot:run -Dspring-boot.run.fork=false
```

## Production Considerations

### 1. Email Configuration
- Use environment variables for credentials
- Configure connection pooling based on load
- Adjust batch size and window for your use case

### 2. Database
- Replace H2 with production database (PostgreSQL, MySQL, etc.)
- Configure appropriate connection pool
- Add database migrations (e.g., Flyway, Liquibase)

### 3. Monitoring
- Add metrics collection (Micrometer, Prometheus)
- Monitor email send rates and failures
- Set up alerts for high error rates

### 4. Error Handling
- Implement dead letter queue for failed emails
- Add circuit breaker for mail server failures
- Configure appropriate retry policies

## Troubleshooting

### Email Not Sending
1. Check SMTP credentials in `application.yml`
2. Verify network connectivity to SMTP server
3. Check application logs for error messages
4. Ensure firewall allows SMTP port (587 or 465)

### Connection Refused
1. Verify application is running: `curl http://localhost:8080/api/users`
2. Check port 8080 is not in use
3. Review application startup logs

### Tests Failing
1. Ensure Java 17 is being used
2. Run `mvn clean` before tests
3. Check for network issues affecting downloads

## Next Steps

1. **Customize Email Templates**: Add HTML email templates
2. **Add More CRUD Operations**: Implement search, pagination
3. **Enhance Error Handling**: Add custom exception handlers
4. **Add Security**: Implement authentication and authorization
5. **API Documentation**: Add Swagger/OpenAPI documentation
6. **Metrics**: Add Micrometer for metrics collection
7. **Docker**: Containerize the application

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Spring WebFlux Guide](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
