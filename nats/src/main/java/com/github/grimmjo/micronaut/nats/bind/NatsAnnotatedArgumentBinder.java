package com.github.grimmjo.micronaut.nats.bind;

import java.lang.annotation.Annotation;

import io.micronaut.core.bind.annotation.AnnotatedArgumentBinder;
import io.nats.client.Message;

/**
 * An interface for nats argument binding based on an annotation
 *
 * @param <A> The annotation that must exist on the argument
 * @author jgrimm
 * @since 1.0.0
 */
public interface NatsAnnotatedArgumentBinder<A extends Annotation>
        extends AnnotatedArgumentBinder<A, Object, Message>, NatsArgumentBinder<Object> {}
