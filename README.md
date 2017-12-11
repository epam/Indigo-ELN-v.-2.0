# Indigo ELN server part

Server part for Indigo ELN. Indigo ELN is the open-source Chemistry Electronic Lab Notebook, provides scientists with
a proven way to create, store, retrieve, and share electronic records of chemistry and biology-related information
in the way of meeting legal, regulatory, technical, and scientific requirements.

## Build dependencies

- Java 1.8
- Maven 3.1+

## Build procedure

Configure DB and external service urls in `src/main/resources/application.properties`.

Execute `mvn spring-boot:run` to start Indigo ELN in development mode.

Execute `mvn clean package -P release` to create production `.war` file.

## Profiles

### Maven profiles

- `dev` - for development use only, activated by default
- `release` - for production use (`mvn clean package -P release`)

See Spring profiles for additional information about development modes.

### Spring profiles

- `dev` - for development use only - enables additional logging
- `cors` - for development use only - enables CORS mappings for UI development
- `release` - for production use

`dev` and `cors` are enabled by default with `dev` Maven profile. `release` is enabled with `release` Maven profile.

See [Spring Boot docs](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) for configuration and installation options.

## Docker image

Execute `docker:build` (`mvn clean package docker:build -P release`).

`Dockerfile` will be placed into `target/docker`.

Example run:
 
`docker run -p 8080:8080 indigoeln`

See Docker options for details.

## Code Style

You should check your code with `CheckStyle` and `FindBugs` maven plugins executing `mvn clean compile` command. 
