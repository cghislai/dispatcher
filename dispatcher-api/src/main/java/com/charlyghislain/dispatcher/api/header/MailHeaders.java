package com.charlyghislain.dispatcher.api.header;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default headers for the message. The values specified here will be overridden if present
 * in a resource bundle.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MailHeaders {

    String from() default "";

    String to() default "";

    String cc() default "";

    String bcc() default "";

    String subject() default "";
}
