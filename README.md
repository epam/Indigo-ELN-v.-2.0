# BingoDB

Simple chemical structure database.

## License

BingoDB is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BingoDB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.

## Build dependencies

- Java 1.8
- Maven 3.1+

## Build procedure

Configure folder for storing files and security properties in `src/main/resources/application.properties`

Execute `mvn spring-boot:run` to start BingoDB in development mode.

Execute `mvn clean package -P release` to create production `.war` file.

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

## Code Style

You should check your code with `CheckStyle` and `FindBugs` maven plugins executing `mvn clean compile` command. 
