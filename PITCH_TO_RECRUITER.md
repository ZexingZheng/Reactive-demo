# Pitch to Recruiter

I chose this project because it shows reactive programming with Spring WebFlux and Project Reactor, uses Kafka for asynchronous user events, and includes a simple front-end so reviewers can run and test the API quickly.

## Technical decisions:
- Non-blocking APIs with WebFlux for better concurrency.
- Reactor Kafka to decouple side effects.
- Email sending executed on a separate scheduler to avoid blocking the reactive event loop.
- A demo profile using H2 (in-memory) so the project is easy to run locally. 

Production improvements (security, secrets, migrations, CI) are noted in the README.

## Run locally:
1. git clone https://github.com/ZexingZheng/Reactive-demo
2. mvn clean package
3. mvn spring-boot:run -Dspring-boot.run.profiles=demo
4. Open http://localhost:9090

## Notes:
The demo profile disables external integrations by default; enable and configure Kafka/SMTP in application properties when needed.