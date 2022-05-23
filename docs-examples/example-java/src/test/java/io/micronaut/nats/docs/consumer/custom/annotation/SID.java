package io.micronaut.nats.docs.consumer.custom.annotation;

// tag::imports[]

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.core.bind.annotation.Bindable;
// end::imports[]

// tag::clazz[]
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Bindable // <1>
public @interface SID {
}
// end::clazz[]
