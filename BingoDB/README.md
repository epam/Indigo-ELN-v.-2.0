# BingoDB

## Build dependencies

- Java 1.6
- Maven 3.1
- GIT

## Maven profiles

- `dev` - for development use only, activated by default
- `release` - for production use (`mvn clean package -P release`)

See [Spring Boot docs](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) for configuration and installation options

## Docker image

Execute `docker:build` (`mvn clean package docker:build -P release`)

`Dockerfile` will be placed into `target/docker`

Example run:
 
`docker run -p 9999:9999 -v c:/bingodb/data:/bingo bingodb`

`-v` is necessary to specify BingoDB storage folder in the host system.

See Docker options for details.
