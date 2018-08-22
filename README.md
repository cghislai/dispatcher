# Dispatcher

A message templating / dispatching library

## Getting started

### Import the api
To make use of the library, the api must be provided to modules requiring it
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
```
The services and api must be available in the classpath of your deployments:
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher</artifactId>
        <type>ejb</type>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

Additionaly, for the management api, provide the following jars
```xml
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-management</artifactId>
        <classifier>classes</classifier> <!-- or <type>war</type> -->
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.charlyghislain.dispatcher</groupId>
        <artifactId>dispatcher-management-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

### Define some message
Messages are defined using the `@MessageDefinition` annotation:
```java

@MessageDefinition(name = "a-first-message",
        dispatchingOptions = {DispatchingOption.MAIL_HTML, DispatchingOption.MAIL_TEXT, DispatchingOption.SMS},
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
Make sure you don't use a scope which will result with a proxified object.

### Configure the paths
In order to locate the velocity templates to render the message, some paths needs to be configured.
Configuration keys and default values are located in the `com.charlyghislain.dispatcher.api.configuration.ConfigConstants`
class. They may be provided using the microprofile config api.

```properties
com.charlyghislain.dispatcher.fileSystemWritableResourcePath=/var/run/templates
com.charlyghislain.dispatcher.resourcesBaseDir=com/example
com.charlyghislain.dispatcher.sharedResourcesSubpath=shared_resources
```
With these values, this library will look for templates for the message in the following locations:
- `/var/run/templates/com/example/a-first-message` on the filesystem
- `com/example/a-first-message` in the classpath

### Write the templates

We defiend 3 dispatching options in the message definition. For the MAIL_HTML option,
we can provide a `mail-html.vm` template in the module classpath:

- `src/main/resources/com/example/a-first-message/mail-html.vm`
- `src/main/resources/com/example/a-first-message/mail-text.vm`
- `src/main/resources/com/example/a-first-message/sms.vm`
- `src/main/resources/com/example/shared_resources/an-image.jpg`

```velocitymarkup
<h1>This is a first message</h1>
<p>The field value is ${first.firstField}</p>
<p>The date value is ${date.format('medium', ${first.dateField})}</p>
<p>An image: <img src="${res.load('an-image.jpg')}"></p>
```

```velocitymarkup
This is a first message

The field value is ${first.firstField}
The date value is ${date.format('medium', ${first.dateField})}
```

For each of these template file, we can append language tags that will be resolved like resource bundles.
So for an html mail message to be rendered for the fr-BE language, the following template files
will be searched for:
- mail-html_fr_BE.vm
- mail-html_fr.vm
- mail-html.vm

### Configure the headers

Mail headers may be defined by annotating the message definition:
```java
@MessageDefinition(name = "a-first-message",
        dispatchingOptions = {DispatchingOption.MAIL_HTML, DispatchingOption.MAIL_TEXT, DispatchingOption.SMS},
        templateContexts = {AFirstMessageContext.class})
@MailHeaders(subject = "A first subject", to="${user.email}")        
public class AFirstMessage {
}
```
Or provided in localized resource bundles properties:
- `src/main/resources/com/example/a-first-message/MailHeaders_en.properties`
```velocitymarkup
from=example@org
subject=A first message
```
Or provided as microprofile config entries, at a global level:
```properties
com.charlyghislain.dispatcher.api.header.MailHeaders#from=example@myorg
```
Or provided as microprofile config entries, at the message level:
```properties
my.fully.qualified.message.definition.package.AFirstMessage.MailHeaders_en#from=example@anotherorg
``` 
They will be resolved in the inverse order (first configuration entries at the message level, last @MailHeaders annotation).
Configuration keys can also be localized using the resource bundle resolution mechanism.

### Dispatch you message
To dispatch your message, first you need a ReadyToBeRenderedMessage:
```java
List<TemplateContextObjects> contextObjects = templateContextsService.createTemplateContexts(message);
MailHeadersTemplate mailMessageHeaders = messageResourcesService.findMailMessageHeaders(message, locale);


ReadyToBeRenderedMessage rtbrm = new ReadyToBeRenderedMessage()
.setMessage(message)
.setAcceptedLocales(Collections.singletonList(locale))
.setContextObjects(contextObjects)
.setDispatchingOptions(Collections.singleton(DispatchingOptions.MAIL_HTML))
.setMailHeadersTemplate(mailMessageHeaders);
```   
Then you can render and dispatch it
```java
RenderedMessage rm = rendererService.renderMessage(rtbrm);
DispatchedMessage dm = dipactherService.dispatchMessage(rm);

assertTrue(dm.getDispatchingResults().get(DispatchingOptions.MAIL_HTML).isSuccess());
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

