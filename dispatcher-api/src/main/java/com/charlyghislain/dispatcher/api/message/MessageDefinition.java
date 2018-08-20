package com.charlyghislain.dispatcher.api.message;


import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface MessageDefinition {

    /**
     * A list of @TemplateContext-annotated classes which will be made available in the velocity context.
     */
    Class<?>[] templateContexts() default {};

    /**
     * A list of dispatching options supported by this message. If using header or footer, they must
     * support at least all options declared here.
     */
    DispatchingOption[] dispatchingOptions() default {DispatchingOption.MAIL_HTML};

    /**
     * Name of the message, which is the folder in which the resources files are fetched.
     * Must be unique, it default to the class simple name.
     * Allowed characters include alphanumeric characters, dash and forward slash.
     * Forward slashes can be used to create subdirectories in the resources folder.
     */
    String name() default "";

    String description() default "";

    /**
     * Reference another message to include before this message content. Referenced messages should have the
     * compositionItem flag set to true and should not include any headers annotation.
     */
    Class<?> header() default Void.class;

    /**
     * Reference another message to include after this message content .Referenced messages should have the
     * * compositionItem flag set to true and should not include any headers annotation.
     */
    Class<?> footer() default Void.class;

    /**
     * Flag indicating if this message should only be included in other messages. They won't be injectable.
     */
    boolean compositionItem() default false;
}
