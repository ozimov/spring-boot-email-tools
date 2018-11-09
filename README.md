# Spring Boot Email Tools
A set of services and tools for sending emails in a **Spring Boot** application using plain text, html or
a template engine to generate dynamic content.

**Source Website:** *[github.com/ozimov/spring-boot-email-tools](http://github.com/ozimov/spring-boot-email-tools/)*<br />

**Latest Release:** *0.6.3* <br />
**Latest Artifacts:** *it.ozimov:spring-boot-email-core*, *it.ozimov:spring-boot-freemarker-email*,
    *it.ozimov:spring-boot-mustache-email*, *it.ozimov:spring-boot-pebble-email*, *it.ozimov:spring-boot-thymeleaf-email* <br />
**Continuous Integration:** <br />
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/it.ozimov/spring-boot-email-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ozimov/spring-boot-email-core)
<br />
[![Build Status](https://travis-ci.org/ozimov/spring-boot-email-tools.svg?branch=master)](https://travis-ci.org/ozimov/spring-boot-email-tools)
[![codecov.io](https://codecov.io/github/ozimov/spring-boot-email-tools/coverage.svg?branch=master)](https://codecov.io/github/ozimov/spring-boot-email-tools?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/7a4364b93df6473fb18a597e900edceb)](https://www.codacy.com/app/roberto-trunfio/spring-boot-email-tools)

![codecov.io](https://codecov.io/github/ozimov/spring-boot-email-tools/branch.svg?branch=master)


## Background

The project relies on a templateless module `it.ozimov:spring-boot-email-core` that provides the core
features (e.g. sending emails, scheduling and prioritizing, persistence). Since it is templateless, it  does not provide
 any implementation of the service to be used to generate the body of the email via template engine.

If you want to use one of the template engines supported by this project (i.e. _Freemarker_,
_Mustache_, _Pebble_ and _Thymeleaf_), you can use the dedicated templatefull
module that is shipped with the core module. The standard naming for the templatefull module is
`it.ozimov:spring-boot-{template_engine_name}-email` (where `{template_engine_name}` is for instance `pebble`).

## Dependency
Latest release is **`0.6.3`**. To use the core module, you can import the following dependency in Maven

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-email-core</artifactId>
    <version>0.6.3</version>
</dependency>
```

To embed the module that includes the _Freemarker_ template engine, you can use the following Maven dependency:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-freemarker-email</artifactId>
    <version>0.6.3</version>
</dependency>
```

for _Mustache_:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-mustache-email</artifactId>
    <version>0.6.3</version>
</dependency>
```

for _Pebble_:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-pebble-email</artifactId>
    <version>0.6.3</version>
</dependency>
```

and for _Thymeleaf_:

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>spring-boot-thymeleaf-email</artifactId>
    <version>0.6.3</version>
</dependency>
```

Remember that if you import the template-full module, the core module should not be required.


## Usage
In your main Spring Boot application, you need to add the annotation `@EnableEmailTools` to
  enable support for all the services and controllers defined in the Spring Boot Email module, e.g.:

```java
@SpringBootApplication
@EnableEmailTools
public class MainApplication  {

    public static void main(final String ... args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```

in you `application.properties` set the configuration needed to send the emails, e.g. if you want to send
the emails using a Gmail account you can set:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=name.surname@gmail.com
spring.mail.password=V3ry_Str0ng_Password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

Plus, the additional properties must be added to prevent using the persistence layer
```properties
spring.mail.scheduler.persistence.enabled=false
spring.mail.scheduler.persistence.redis.embedded=false
spring.mail.scheduler.persistence.redis.enabled=false
```

To send an email, use the ``EmailService`` in your Spring Boot application. E.g.


```java
@Autowired
public EmailService emailService;

public void sendEmailWithoutTemplating(){
   final Email email = DefaultEmail.builder()
        .from(new InternetAddress("cicero@mala-tempora.currunt", "Marco Tullio Cicerone "))
        .to(Lists.newArrayList(new InternetAddress("titus@de-rerum.natura", "Pomponius Attĭcus")))
        .subject("Laelius de amicitia")
        .body("Firmamentum autem stabilitatis constantiaeque eius, quam in amicitia quaerimus, fides est.")
        .encoding("UTF-8").build();

   emailService.send(email);
}
```


The previous code will send a plain text message. To obtain some more dynamic fancy emails, you have two options:
_i)_ the former and easier-to-use is to use a templatefull module (e.g. based on Freemarker);
_ii)_ the latter (which requires some effort on your side) needs an implementation of the
interface **`it.ozimov.springboot.templating.mail.service.TemplateService`**.

The aforementioned interface requires a component that implements the following method

```java
String mergeTemplateIntoString(String templateReference, Map<String, Object> model)
            throws IOException, TemplateException;
```

Assuming you opted for one of the previous options, just put the template in the required folder
 (e.g. ``templates`` under ``resourses``) and try to execute the following code (it works with _Freemarker_):

```java
@Autowired
public EmailService emailService;

public void sendEmailWithTemplating(){
   Arrays.asList(new Cospirator("cassius@sic-semper.tyrannis", "Gaius Cassius Longinus"),
            new Cospirator("brutus@sic-semper.tyrannis", "Marcus Iunius Brutus Caepio"))
        .stream.forEach(tyrannicida -> {
       final Email email = DefaultEmail.builder()
            .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
            .to(Lists.newArrayList(new InternetAddress(tyrannicida.getEmail(), tyrannicida.getName())))
            .subject("Idus Martii")
            .body("")//Empty body
            .encoding("UTF-8").build();
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

where the template ``idus_martii.ftl`` is a _Freemarker_ file like:

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


The following example shows how to send and email that includes an inline image.


```java
@Autowired
public EmailService emailService;

public void sendEmailWithTemplatingAndInlineImage(){
       final Email email = DefaultEmail.builder()
            .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
            .to(Lists.newArrayList(new InternetAddress("brutus@sic-semper.tyrannis", "Marcus Iunius Brutus Caepio")))
            .subject("Idus Martii")
            .body("")//Empty body
            .encoding("UTF-8").build();
       //Defining the model object for the given Freemarker template
       final Map<String, Object> modelObject = new HashMap<>();
       final File imageFile = //load your picture here, e.g. "my_image.jpg"
       modelObject.put("tyrannicida", tyrannicida.getName());

       final InlinePicture inlinePicture = DefaultInlinePicture.builder()
                               .file(imageFile)
                               .imageType(ImageType.JPG)
                               .templateName("my_image.jpg").build());

       emailService.send(email, "idus_martii.ftl", modelObject, inlinePicture);
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
final InlinePicture inlinePicture = DefaultInlinePicture.builder()
        .file(imageFile)
        .imageType(ImageType.JPG)
        .templateName("images/my_image.jpg").build());
```

This is required to set the a proper content-id.

## Email scheduling

The library supports email scheduling, but since version _0.6.3_ the scheduler is disabled by default. To enable 
email scheduling, the following property has to be provided:
 
```properties
spring.mail.scheduler.enabled=true
```

Email can be set in different queues, from the one with
 highest priority to the least important. Priority 1 is the highest.

To define the number of priority levels to be used in the scheduler, 
just add in the `application.properties` the following line:

```properties
spring.mail.scheduler.priorityLevels=5
```

If not provided, by default 10 priority levels are considered.

Scheduling an email is actually easy and the `EmailSchedulerService` allows to schedule an email with or without
the use of a template engine.

In order to schedule a plain text email, just create your service (or controller) where you autowire 
the service `EmailSchedulerService` and call a method `scheduleEmail` defined as in the following example

```java
@Service
public void MyEmailSenderService {

    @Autowired
    private EmailSchedulerService EmailSchedulerService;
    
    
    public void scheduleEmail() throws CannotSendEmailException {
        final Email mimeEmail = DefaultEmail.builder()
                                  .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
                                  .to(Lists.newArrayList(new InternetAddress(tyrannicida.getEmail(), tyrannicida.getName())))
                                  .subject("Idus Martii")
                                  .body("Sic semper...")
                                  .encoding("UTF-8")
                                  .build();
        final OffsetDateTime scheduledDateTime = OffsetDateTime.now().plusDays(1);
        final int priorityLevel = 1;
      EmailSchedulerService.schedule(mimeEmail, scheduledDateTime, priorityLevel);
    }
}
```

Here we go, by calling schedulerEmail() an email has been scheduled to be sent after one day.
When scheduling emails, observe that **`OffsetDateTime` must be** used with **UTC**, so do not forget to convert it if you
use a different zone offset.

To schedule an email with a template and inline images, just call a new method called `scheduleEmailWithTemplate()`

```java
@Service
public void MyEmailWithTemplateSenderService {

    @Autowired
    private EmailSchedulerService EmailSchedulerService;
    
    
    public void scheduleEmailWithTemplate() throws CannotSendEmailException {
        final Email mimeEmail = DefaultEmail.builder()
                                  .from(new InternetAddress("divus.iulius@mala-tempora.currunt", "Gaius Iulius Caesar"))
                                  .to(Lists.newArrayList(new InternetAddress(tyrannicida.getEmail(), tyrannicida.getName())))
                                  .subject("Idus Martii")
                                  .body("")//Empty body
                                  .encoding("UTF-8")
                                  .build();
       //Defining the model object for the given Freemarker template
       final Map<String, Object> modelObject = new HashMap<>();
       final File imageFile = //load your picture here, e.g. "my_image.jpg"
       modelObject.put("tyrannicida", tyrannicida.getName());

       final InlinePicture inlinePicture = DefaultInlinePicture.builder()
                               .file(imageFile)
                               .imageType(ImageType.JPG)
                               .templateName("my_image.jpg").build();
        final OffsetDateTime scheduledDateTime = OffsetDateTime.now().plusDays(1);
        final int priorityLevel = 1;
      
        EmailSchedulerService.schedule(mimeEmail, scheduledDateTime, priorityLevel, 
            "idus_martii.ftl", modelObject, inlinePicture);
    }
    
}
```

## Persistence
Persistence has been introduced in version `0.4.0`. Persistence is mainly of interest if the scheduler is used, therefore
it can be enabled only if the scheduler is enabled.

The persistence layer is optional, thus needs to be activated. The default implementation is fully based on embedded REDIS.
To enable the default persistence layer just add the additional properties in your `application.properties` file:

```properties
spring.mail.scheduler.persistence.enabled=true
spring.mail.scheduler.persistence.redis.enabled=true
spring.mail.scheduler.persistence.redis.embedded=true
spring.mail.scheduler.persistence.redis.host=localhost
spring.mail.scheduler.persistence.redis.port=6381
spring.mail.scheduler.persistence.redis.settings=
```

I recommend to specify in the REDIS settings at least the `appendfilename` and `dir` properties,
so that you know where the append file is placed and which name it uses. For instance do:

```properties
spring.mail.scheduler.persistence.redis.settings=appendfilename email_appendonly.aof, dir /Users/your_username/Downloads
```

By default we have the setting `appendonly yes` and `appendfsync everysec`. Feel free to override them or fine tune them 
according with your needs.


Clearly, you can provide your own persistence layer by implementing the `PersistenceService` interface. You can also
 use your REDIS implementation, but this will require extra coding on your side.


Observe that the persistence layer makes the emails being stored to be reloaded on application startup if not yet sent.
In particular, the emails are loaded when scheduler is constructed. 

###Impact of the Persistence layer on the default priority-based scheduler
The default scheduler is `PriorityQueueEmailSchedulerService`, which by default stores everything in memory. Clerarly, having
thousands email being scheduled, storing everything in memory could drive to a potential `OutOfMemoryException`. 
Enabling the persistence layer should allow to use REDIS for persisting scheduled emails. Anyway, you may want to
customize the behavior of the scheduler when interacting with the persistence layer, you can use the following params:

```properties
spring.mail.scheduler.persistence.desiredBatchSize=200
spring.mail.scheduler.persistence.minKeptInMemory=100
spring.mail.scheduler.persistence.maxKeptInMemory=1000
```

The first defines the maximum amount of emails being loaded from the persistence layer when a slot is available in the
priority queues; the second amount is the wish for the minimum amount of emails available in memory: the third defines 
the amount of emails to be kept in memory. Clearly, these two values impact the response time of the scheduler. 
The less you store in memory, the more it takes to send the next email. The smaller
is the batch size, the higher the times you interact with the persistence layer.

## Customize email logging
Very often, you want to log the email that you just sent or scheduled, but you would like to avoid a full 
`toString` of the given email object. For instance, you may want to anonymize an email address, or to ignore custom headers.
Here follows a list of properties you can use with some examples:

```properties
spring.mail.logging.enabled=true

spring.mail.logging.strategy.from=PLAIN_TEXT
spring.mail.logging.strategy.replyTo=HIDDEN
spring.mail.logging.strategy.to=FULL_TEXT_FROM_COMMERCIAL_AT,
spring.mail.logging.strategy.cc=HIDDEN
spring.mail.logging.strategy.bcc=HIDDEN
spring.mail.logging.strategy.subject=PLAIN_TEXT
spring.mail.logging.strategy.body=FIRST_DOZEN_THEN_STARS
spring.mail.logging.strategy.attachments=HIDDEN
spring.mail.logging.strategy.encoding=HIDDEN
spring.mail.logging.strategy.locale=HIDDEN
spring.mail.logging.strategy.sentAt=STANDARD_DATE_FORMAT_WITH_ZONE_ID
spring.mail.logging.strategy.receiptTo=HIDDEN
spring.mail.logging.strategy.depositionNotificationTo=HIDDEN
spring.mail.logging.strategy.ignore.customHeaders=true
spring.mail.logging.strategy.ignore.nullAndEmptyCollections=true
```

Allowed logging strategies are defined in the enum `it.ozimov.springboot.mail.logging.LoggingStrategy`.
Do not pretend to apply a date-only strategy to an email address, or an email address-only strategy to 
a text field. Usage should be straightforward.

## Future plans

See open issues.

**Any contribution is welcome (and warmly encouraged).**


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

## How to open an issue

Are you experiencing an issue? Please, post a question on _StackOverflow_ or open an issue on GitHub.

Issues that are not reporting a minimal set of info to reproduce the bug will be closed with no further comments.

Information that should be provided for investigations:
- version used
- pom.xml
- application.properties
- exception stacktrace
- Are the provided examples run with success?


==============================================

[![forthebadge](http://forthebadge.com/images/badges/built-by-developers.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/built-with-love.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/pretty-risque.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/makes-people-smile.svg)](http://forthebadge.com)
