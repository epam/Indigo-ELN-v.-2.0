# Indigo ELN server part

## Build dependencies

- Java 1.8
- Maven 3.1
- GIT

## Profiles

### Maven profiles

- `dev` - for development use only, activated by default
- `release` - for production use (`mvn clean package -P release`)

See Spring profiles for additional information about development modes.

### Spring profiles

- `dev` - for development use only - enables additional logging
- `cors` - for development use only - enables CORS mappings for UI development
- `release` - for production use

`dev` and `cors` are enabled by default with `dev` Maven profile. `release` is enabled with `release` Maven profile

See [Spring Boot docs](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) for configuration and installation options

## Docker image

Execute `docker:build` (`mvn clean package docker:build -P release`)

`Dockerfile` will be placed into `target/docker`

Example run:
 
`docker run -p 8080:8080 indigoeln`

See Docker options for details.

## Code Style

Before start edit code, you should:

- Install plugin `SonarLint` and config its serverUrl as `https://sonar.epam.com/sonarqube/overview?id=com.epam.indigoeln`
- Import to `IndigoELN/code-style.xml` to settings `Editor/Code Style`
- Enable `Eslint` with `IndigoELN/.eslintrc.json` in `/Languages &  Frameworks/JavaScript/Code Quality Tools` 
