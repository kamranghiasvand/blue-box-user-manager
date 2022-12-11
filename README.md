# user-manager
An OAuth2 authentication and authorization Spring boot application based on Spring Security.
Expose REST api to communicate with. Uses H2 as the default database.
Stateless application with capability to extend quickly in order to use in microservice architecture  

# Installation Guide
### Install JAVA 17
1. Download and install JAVA 17 from [Here](https://www.azul.com/downloads/?version=java-17-lts&package=jdk)
2. Set environment variable $JAVA_HOME to the path where Java has been installed 
### Install Maven
1.Download and install maven from [Here](https://maven.apache.org/download.cgi)

## building project
1. run `mvn clean package`
## Creating Docker Image
1. Download and install Docker from [Here](https://www.docker.com/products/docker-desktop/)
2. run `docker build -t user-manager .`
  