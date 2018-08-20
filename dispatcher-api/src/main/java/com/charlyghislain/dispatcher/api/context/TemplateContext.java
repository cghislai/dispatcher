package com.charlyghislain.dispatcher.api.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a context object to be used in a velocity template.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TemplateContext {

    /**
     * The key for which to bind this context object instance. Fields declared in this object
     * will be available as ${key.field} in the template.
     */
    String key();

    /**
     * Optional description;
     */
    String description() default "";

    /**
     * Set this flag to true if a CDI producer will be available to instantiate this class which the qualifier
     * {@link ProducedTemplateContext}
     * If this context object is not produced, instances must be provided at runtime. If it is, then a
     * {@link ProducedTemplateContext}-qualified instance is expected to be resolved at runtime.
     */
    boolean produced() default false;
}
