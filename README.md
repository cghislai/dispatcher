# Dispatcher

A message rendering / dispatching library.

The goal is to handle the message rendering and dispatching logic, offering a simple API
to make sure a communication message reaches its recipients, whatever language or communication channel preference
they may have.

## Getting started

This repository contains an example application.  See below for usage. This section focus on integrating the library
in an existing project.

### Import the api
To make use of the library, the api must be provided to modules requiring it.
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-api</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
```
The services and api must be available in the classpath of your deployments:
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher</artifactId>
        <type>ejb</type>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-api</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
```

Additionaly, for the management api, provide the following jars.
The management module exposes REST service to allow the edition of message templates located on a filesystem. This is
considered alpha stage.
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-management</artifactId>
        <classifier>classes</classifier> <!-- or <type>war</type> -->
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-management-api</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
```

### Define some message
Messages are defined using the `@MessageDefinition` annotation:
```java

@MessageDefinition(name = "a-first-message",
        renderingOptions = {RenderingOption.LONG_HTML, RenderingOption.LONG_TEXT, RenderingOption.SHORT_TEXT},
        templateContexts = {AFirstMessageContext.class})
public class AFirstMessage {
}
```
They reference template contexts, which are defined using the `@TemplateContext` annotation:
```java

@TemplateContext(key = "first", produced = true)
public class AFirstMessageContext {

    @TemplateField(description = "A first template field", example = "A first value")
    private String firstField;

    @TemplateField(description = "A date field", example = "2018-08-20T14:13:11")
    private Date dateField;

    // getter/setters
}

```

A message defined like above may be injected in your application using the `@Message` annotation:
```java
@Inject
@Message(AFirstMessage.class)
private DispatcherMessage firstMessage;
```

The template context, which provides variables to reference from the template, may be produced using the
`@ProducedTemplateContext` annotation:
```java
@Produces
@Dependent
@ProducedTemplateContext
public AFirstMessageContext produceAFirstMessageContext(HttpServletRequest servletRequest) {
    // ...
}
```
Make sure you don't use a scope which will result with a proxified object. Attempt to resolve TemplateContext instances
via CDI will only be made if the @TemplateContext annotation has the 'produced' parameter set to true. TemplateContext
instances may also be provided at runtime. 

### Configure the paths
In order to locate the velocity templates to render the message, some paths needs to be configured.
Configuration keys and default values are located in the `com.charlyghislain.dispatcher.api.configuration.ConfigConstants`
class. They may be provided using the microprofile config api.

```properties
com.charlyghislain.dispatcher.fileSystemWritableResourcePath=/var/run/templates
com.charlyghislain.dispatcher.resourcesBaseDir=com/example
com.charlyghislain.dispatcher.sharedResourcesSubpath=shared_resources
```
With these values, this library will look for templates for the example message above in the following locations:
- `/var/run/templates/com/example/a-first-message` on the filesystem
- `com/example/a-first-message` in the classpath

### Write the templates

We defiend 3 rendering options in the message definition. Each rendering option has a message template filename that
will be resolved. For instance, we can create the following velocity template files and resources, in the module classpath: 

- `src/main/resources/com/example/a-first-message/long-html.vm`
- `src/main/resources/com/example/a-first-message/long-text.vm`
- `src/main/resources/com/example/a-first-message/short-text.vm`
- `src/main/resources/com/example/shared_resources/an-image.jpg`

```velocitymarkup
<h1>This is a first html message</h1>
<p>The field value is ${first.firstField}</p>
<p>The date value is ${date.format('medium', ${first.dateField})}</p>
<p>An image: <img src="${res.load('an-image.jpg')}"></p>
```

```velocitymarkup
This is a first text message

The field value is ${first.firstField}
The date value is ${date.format('medium', ${first.dateField})}
```

For each of these template file, we can append language tags that will be resolved like resource bundles.
So for an html mail message to be rendered for the fr-BE language, the following template files
will be resolved, in order, until one is found:
- mail-html_fr_BE.vm
- mail-html_fr.vm
- mail-html.vm

### Configure the headers

Message headers may vary depending on the dispatching option - the channel used to contact the recipient.
The only dispatching option worked on currently is the MAIL one.

Mail headers can be provided by multiple means. By annotating the message type:

```java
@MessageDefinition(name = "a-first-message",
        renderingOptions = {DispatchingOption.LONG_HTML, DispatchingOption.LONG_TEXT, DispatchingOption.SHORT_TEXT},
        templateContexts = {AFirstMessageContext.class})
@MailHeaders(subject = "A first subject", to="${user.email}")        
public class AFirstMessage {
}
```
Or provided in localized resource bundles properties:
- `src/main/resources/com/example/a-first-message/MailHeaders_en.properties`
```properties
from=example@org
subject=A first message
```
Or provided as microprofile config entries, at a global level:
```properties
com.charlyghislain.dispatcher.api.header.MailHeaders#from=noreply@myorg
com.charlyghislain.dispatcher.api.header.MailHeaders#bcc=sent-mails@myorg
```
Or provided as microprofile config entries, at the message level:
```properties
my.fully.qualified.message.definition.package.AFirstMessage.MailHeaders_en#from=example@anotherorg
``` 

Each header value will be resolved in the inverse order, so a value provided as a microprofile config parameter
will override one provided at the message annotation level. Additionally, as you can see, values provided as config parameters
may append a localization suffix, that will be resolved like for the template files.

### Dispatch your message
To dispatch your message, first you construct a ReadyToBeRenderedMessage instance and render it
```java
List<TemplateContextObjects> contextObjects = templateContextsService.createTemplateContexts(message);
ReadyToBeRenderedMessage rtbrm = ReadyToBeRenderedMessageBuilder.newBuider(message)
                .acceptMailDispatching()
                .acceptLocale(locale)
                .withContext(contextObjects)
                .build();
RenderedMessage renderedMessage = rendererService.renderMessage(rtbrm);

```   
Then you can dispatch it and collect the results.
```java
DispatchedMessage dispatchedMessage = dipactherService.dispatchMessage(renderedMessage);
boolean hasDispatchedHtmlMail = dispatchedMessage.getDispatchingResultList().stream()
    .filter(result->result.getDispatchingOption() == MAIL)
    .filter(result->result.getRenderingOption() == LONG_HTML)
    .filter(DispatchingResult::isSuccess)
    .findAny().isPresent();
assertTrue(hasDispatchedHtmlMail);
``` 

### Examples
Building the project with the `example` profile activated produces a payaramicro executable jar
deploying an example application.

The example application exposes rest resources at `http://localhost:8080/example/{messageid}`, where
messageId is `a` or `b`, corresponding to one of the two defined messages. An `user` query parameter
may be provided to alter the authenticated user name.

Each message resource expose the following endpoints:
- GET mail/html
- GET mail/text
- GET mail/headers
- GET mail/mime
- GET sms
- POST mail/mime/send

The last resource accepts a `to` query parameter to append a recipient. 
You may need to configure some properties to make it work correctly. For instance, you can supply the following
microporifle config properties as system properties:
```
-Dmail.host=smtp.gmail.com 
-Dmail.auth.user=myuser
-Dmail.auth.password= 
-Dmail.smtp.port=587
-Dcom.charlyghislain.dispatcher.mail.transportEnabled=true 
-Dcom.charlyghislain.dispatcher.api.header.MailHeaders#from=myuser@gmail.com 
```
