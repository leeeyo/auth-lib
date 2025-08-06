# Authentication Library

A flexible authentication library built with Spring Boot that supports both LDAP and Database authentication methods.

## Features

- Dual authentication support:
  - LDAP-based authentication
  - Database-based authentication
- User management with REST API
- Role-based access control
- Session management
- Web interface with Thymeleaf templates
- Comprehensive logging system
- Secure password handling
- Integration testing support for both authentication methods

## Tech Stack

- Java 17
- Spring Boot 3.4.1
- Spring Data JPA
- Spring LDAP
- Spring Security
- Thymeleaf
- MySQL/H2 Database
- Gradle
- JUnit 5
- JaCoCo for test coverage
- SonarQube for code quality
- Lombok

## Prerequisites

- JDK 17 or later
- MySQL (for database authentication)
- LDAP Server (for LDAP authentication)
- Gradle 7.x or later

## Configuration

### Application Properties

The application can be configured through `application.properties`:

```properties
# Core Settings
spring.application.name=auth
server.port=9090

# Authentication Type (ldap or db)
authentication.type=ldap

# Database Configuration (for db authentication)
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>

# LDAP Configuration (for ldap authentication)
spring.ldap.url=ldap://localhost:389/dc=maxcrc,dc=com
spring.ldap.base=dc=maxcrc,dc=com
spring.ldap.username=cn=Manager,dc=maxcrc,dc=com
spring.ldap.password=<your-ldap-password>
```

## Building

```bash
./gradlew clean build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Skip integration tests
./gradlew test -PskipIntegrationTests=true

# Generate test coverage report
./gradlew jacocoTestReport
```

## Running the Application

### Using Gradle

```bash
./gradlew bootRun
```

### Using Distribution Scripts

The application includes distribution scripts for different platforms:

#### Windows
```bash
bin/start.bat
```

#### Linux/Mac
```bash
bin/start.sh
```

The application will be available at `http://localhost:9090/login`

## Distribution

The project creates distributable packages including:

- JAR file
- Configuration files
- Start/stop scripts
- Log directory structure

Generated distributions can be found in:
- `build/distributions/Project1-1.0.0.zip`
- `build/distributions/Project1-1.0.0.tar`

## API Endpoints

### User Management

```
GET    /api/v1/users          # Fetch all users
POST   /api/v1/users          # Create new user
PUT    /api/v1/users/{id}     # Update user
DELETE /api/v1/users/{id}     # Delete user
```

## Web Interface

- `/login` - Login page
- `/dashboard` - User dashboard
- `/logout` - Logout endpoint

## Testing

The project includes comprehensive test suites:

- Unit tests
- Integration tests for Database authentication
- Integration tests for LDAP authentication
- Controller tests

Test configurations are available for both authentication methods:
- `application-test-db.properties`
- `application-test-ldap.properties`

## Logging

Logs are configured with rolling file policy:
- Location: `logs/app.log`
- Rolling policy: Daily with size limits
- Maximum file size: 10MB
- Total size cap: 100MB
- History: 30 days

## Development

### Code Quality

The project uses:
- SonarQube for code quality analysis
- JaCoCo for test coverage reporting
- Checkstyle for code style enforcement

### Publishing

The project can be published to a Maven repository:

```gradle
./gradlew publish
```

## License

This project is private and proprietary software of DigiTechNova.

---

For more information or support, contact the development team at DigiTechNova.
