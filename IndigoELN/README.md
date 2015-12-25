## Indigo ELN

### Build dependencies

- Java 1.8
- Maven 3.1
- GIT

### Available profiles

- dev
- release

### Build and run

Run project in `dev` mode: `mvn clean tomcat7:run`

Build `release` package: `mvn clean package -P release`
