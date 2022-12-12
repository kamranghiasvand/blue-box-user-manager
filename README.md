# user-manager
An OAuth2 authentication and authorization Spring boot application based on Spring Security.
Expose REST api to communicate with. Uses H2 as the default database.
Stateless application with capability to extend quickly in order to use in microservice architecture  

# Building the project
1. Download and install JAVA 17 from [Here](https://www.azul.com/downloads/?version=java-17-lts&package=jdk)
2. Set environment variable $JAVA_HOME to the path where Java has been installed
3. Download and install maven from [Here](https://maven.apache.org/download.cgi)
4. run `mvn clean package`
## Running as docker container
1. Download and install Docker from [Here](https://www.docker.com/products/docker-desktop/)
2. run `docker build -t user-manager .`
3. run `docker run -p 8080:8080  user-manager`

# Swagger UI
you can see swagger documentation here:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)