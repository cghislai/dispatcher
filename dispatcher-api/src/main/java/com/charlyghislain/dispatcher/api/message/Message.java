package com.charlyghislain.dispatcher.api.message;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier used to inject DispatcherMessage instances.
 * Only messages which are not composition items will be made available for injection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
@Qualifier
public @interface Message {
    Class<?> value() default Void.class;
}
