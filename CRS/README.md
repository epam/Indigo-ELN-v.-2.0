## Compound Registration and Search Service

### Database

To install database schema, see `database/README.md`

To configure database connection, use `src/main/resources/crs.properties`

### Client

To use CRS client, build CRS with Maven, and use `target/crs-client.jar`.  
Crs client depends on:

 - `spring-web:3.x`
 - `commons-httpclient:3.x`
