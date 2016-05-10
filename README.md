# spring-boot-email-tools
A set of services and tools for sending emails in a **Spring Boot** application using *Freemarker* template engine.

**Source Website:** *[github.com/ozimov/spring-boot-email-tools](http://github.com/ozimov/spring-boot-email-tools/)*<br />

**Latest Release:** *0.2.0*<br />
**Latest Artifacts:** *it.ozimov:/spring-boot-email-tools* <br />
**Continuous Integration:**<br />
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/it.ozimov/spring-boot-email-tools/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ com.github.ozimov/spring-boot-email-tools)
<br />
[![Build Status](https://travis-ci.org/ozimov/spring-boot-email-tools.svg?branch=master)](https://travis-ci.org/ozimov/spring-boot-email-tools)
[![codecov.io](https://codecov.io/github/ozimov/spring-boot-email-tools/coverage.svg?branch=master)](https://codecov.io/github/ozimov/spring-boot-email-tools?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/7a4364b93df6473fb18a597e900edceb)](https://www.codacy.com/app/roberto-trunfio/spring-boot-email-tools)

![codecov.io](https://codecov.io/github/ozimov/spring-boot-email-tools/branch.svg?branch=master)


## Dependency
Latest release is:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-email-tools</artifactId>
    <version>0.2.0</version>
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


To send an email, use the ``EmailService`` in your Spring Boot application. E.g.


```java
@Autowired
public EmailService emailService;

public void sendEmailWithoutTemplating(){
   final Email email = EmailImpl.builder()
        .from(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone "))
        .to(Lists.newArrayList(new InternetAddress("titus@de-rerum.natura", "Pomponius Attĭcus")))
        .subject("Laelius de amicitia")
        .body("Firmamentum autem stabilitatis constantiaeque eius, quam in amicitia quaerimus, fides est.")
        .encoding(Charset.forName("UTF-8")).build();

   emailService.send(email);
}
```


The previous code will send a plain text message. To obtain some more dynamic fancy emails, you can use the Freemarker template engine. Just put the template in the required folder (e.g. ``templates`` under ``resourses``) and


```java
@Autowired
public EmailService emailService;

public void sendEmailWithTemplating(){
   Arrays.asList(new Cospirator("cassius@sic-semper.tyrannis", "Gaius Cassius Longinus"),
            new Cospirator("brutus@sic-semper.tyrannis", "Marcus Iunius Brutus Caepio"))
        .stream.forEach(tyrannicida -> {
       final Email email = EmailImpl.builder()
            .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
            .to(Lists.newArrayList(new InternetAddress(tyrannicida.getEmail(), tyrannicida.getName())))
            .subject("Idus Martii")
            .body("")//Empty body
            .encoding(Charset.forName("UTF-8")).build();
        //Defining the model object for the given Freemarker template
        final Map<String, Object> modelObject = new HashMap<>();
        modelObject.put("tyrannicida", tyrannicida.getName());

       emailService.send(email, "idus_martii.ftl", modelObject);
   };
}

private static class Cospirator {
  private String email;
  private String name;
  public Cospirator(final String email, final String name){
    this.email = email;
    this.name = name;
  }

  //getters
}
```

where the template ``idus_martii.ftl`` is a Freemarker file like:

```html
<!doctype html>
<html>
	<body>
		<p>
			Tu quoque, <em>${tyrannicida}</em>!
		</p>
	</body>
</html>
```


The following example shows how to send emails that include an inline image.


```java
@Autowired
public EmailService emailService;

public void sendEmailWithTemplatingAndInlineImage(){
       final Email email = EmailImpl.builder()
            .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
            .to(Lists.newArrayList(new InternetAddress("brutus@sic-semper.tyrannis", "Marcus Iunius Brutus Caepio")))
            .subject("Idus Martii")
            .body("")//Empty body
            .encoding(Charset.forName("UTF-8")).build();
       //Defining the model object for the given Freemarker template
       final Map<String, Object> modelObject = new HashMap<>();
       final File imageFile = //load your picture here, e.g. "my_image.jpg"
       modelObject.put("tyrannicida", tyrannicida.getName());

       final InlinePicture inlinePicture = InlinePictureImpl.builder()
                               .file(imageFile)
                               .imageType(ImageType.JPG)
                               .templateName("my_image.jpg").build());

       emailService.send(email, "idus_martii.ftl", modelObject, inlinePicture);
}

  //getters
}
```

where the template ``idus_martii.ftl`` is a Freemarker file like:

```html
<!doctype html>
<html>
	<body>
		<p>
			<img src="my_image.jpg" />
		</p>
	</body>
</html>
```

be sure that the name provided in the ``InlinePicture`` matches with the one used in the template file path included, if
any was set. This means that if in the template you have ``<img src="images/my_image.jpg" />`` then the definition has to be
changed as follows:

```java
final InlinePicture inlinePicture = InlinePictureImpl.builder()
        .file(imageFile)
        .imageType(ImageType.JPG)
        .templateName("images/my_image.jpg").build());
```

This is required to set the a proper content-id.

## Email scheduling

The library supports email scheduling. Email can be set in different queues, from the one with
 highest priority to the least important. Priority 1 is the highest.

To define the number of priority levels, just add in the `application.properties` the following line:

```properties
spring.mail.scheduler.priorityLevels=10
```

Scheduling an email is actually easy. To schedule an email, just resort to the service
`PriorityQueueSchedulerService`. The service allows to schedule an email with or without
the use of a template engine...

```java
@Autowired
private PriorityQueueSchedulerService scheduler;


public void schedule (final Email mimeEmail, final OffsetDateTime scheduledDateTime, final int priorityLevel){
  scheduler.schedule(mimeEmail, scheduledDateTime, priorityLevel);
}
```

Observe that `OffsetDateTime` must be used with UTC.


Here we go, an email has been scheduled.

To schedule an



## Future plans

Here are listed the backlog for the features to be added to the library in the near future:
* Quartz scheduler for handling newsletter
* Email queuing using priorities

Any contribution is welcome.


## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


==============================================
[![forthebadge](http://forthebadge.com/images/badges/built-by-developers.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/built-with-love.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/pretty-risque.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/makes-people-smile.svg)](http://forthebadge.com)