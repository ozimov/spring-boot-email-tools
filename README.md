# SpringBootEmailTools
A set of  (rest) services and tools for sending emails in a Spring Boot application using templating


## Dependency

```xml
<dependency>
    <groupId>spring-boot-utils</groupId>
    <artifactId>spring-boot-email</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```


## Usage
In your main Spring Boot application, you may need to add an explicit reference
to scan for all the services and controllers defined in the Spring Boot Email module, e.g.:

```java
package com.myapplication;

@SpringBootApplication
@ComponentScan(basePackages = {"com.myapplication", "open.springboot.mail"})
public class MainApplication  {

    public static void main(final String... args) {

    }
}
```


in you application.yml set the configuration needed to send the emails, e.g. if you want to send
the emails using a Gmail account you can set:

```yml
spring.mail.host: smtp.gmail.com
spring.mail.port: 587
spring.mail.username: name.surname@gmail.com
spring.mail.password: V3ry_Str0ng_Password
spring.mail.properties.mail.smtp.auth: true
spring.mail.properties.mail.smtp.starttls.enable: true
spring.mail.properties.mail.smtp.starttls.required: true
```

If you don't like the default path for the controllers, you can easily replace
them in the application.yml. Otherwise, the default path is used
