# Security-Service

This project is a **Spring Boot microservice** that handles authentication and authorization using **Keycloak** as the Identity Provider. It is designed to work in a **Dockerized** environment.

## ✨ Features

- OAuth2 Resource Server integration
- JWT token validation via Keycloak
- Secure microservices with minimal config
- Dockerized Keycloak server setup
- Spring Boot 3.x ready

## 📦 Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose
- Git

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/security-service.git
cd security-service
```

### 2. Start Keycloak using Docker

A docker-compose.yml file is provided to quickly spin up Keycloak:

```bash
docker-compose up -d
```

**This will:**
- Pull the Keycloak image
- Start Keycloak on http://localhost:8085
- Create an admin user:
  - Username: security
  - Password: security

### 3. Keycloak Configuration

1. Open the Keycloak admin console:
   - http://localhost:8085/admin
   - Login with security/security

2. Create a realm named: `securityrealm`

3. Create a client:
   - Client ID: `security-service`
   - Access Type: confidential
   - Set "Direct Access Grants Enabled" to ON
   - Save and get the client secret

## 🔗 Service URLs

| Service | URL |
|---------|-----|
| Keycloak Admin Console | http://localhost:8085/admin |
| OpenID Configuration | http://localhost:8085/realms/securityrealm/.well-known/openid-configuration |

## 🧪 Kafka Setup

Kafka is used for event-based messaging (e.g., `user.created` topic) in this microservice architecture.

### 1. Start Kafka

Kafka and Zookeeper can be started using Docker (included in `docker-compose.yml`):

```bash
docker-compose up -d
```

This brings up:
- Zookeeper on port 2181
- Kafka broker on port 9092

### 2. Check If Topic Exists

Run this inside the Kafka container:

```bash
docker exec -it <kafka-container-name> kafka-topics --list --bootstrap-server localhost:9092
```

### 3. Create Topic (if needed)

To create the `user.created` topic:

```bash
docker exec kafka kafka-topics --create --topic user.created --bootstrap-server kafka:29092 --partitions 1 --replication-factor 1
```

> Replace `<kafka-container-name>` with the Kafka container ID (use `docker ps` to find it).

### 4. Kafka Config in Spring Boot

Add the following to your `application.properties` or `application.yml`:

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=security-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 5. (Optional) Enable Kafka Debug Logging

Add this line for more detailed logs during development:

```properties
logging.level.org.apache.kafka=DEBUG
```

