## BingoDB

REST API service for [Bingo NoSQL](http://lifescience.opensource.epam.com/bingo/bingo-nosql.html) 

### Development

For development, simply run `Application` class.

### Deployment

For deployment, BingoDB should be installed as service. 

See `config/application.properties` in deployment package for usual properties.

Application server requirements:

- 1GB of free memory
- Java Runtime Environment version 8 (Better use the latest version of Java)

#### Unix service
`bingodb.jar` can act as usual init.d script. For more information see [Spring Boot docs](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment-service)

#### Windows service 
Use `bingodb.exe` (install|uninstall|start|stop|restart) in deployment package. For more information see [winsw docs](https://github.com/kohsuke/winsw)
