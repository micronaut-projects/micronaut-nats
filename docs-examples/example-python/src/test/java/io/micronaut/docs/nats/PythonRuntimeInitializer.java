package io.micronaut.docs.nats;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import org.graalvm.polyglot.Context;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO(python): the executable method processors of {@code @MessageListener} beans (the NATS consumer
 * advices) are created before the {@code @Context} beans are initialized, and their constructors
 * instantiate every {@code NatsArgumentBinder} and {@code NatsMessageSerDes} bean of the context.
 * A Python binder, serdes or {@code @NatsListener} bean would therefore be instantiated before the
 * GraalPy runtime exists ("GraalPy context has not been initialized"). Creating the GraalPy context
 * when the first bean of the application context is created makes sure the runtime is installed
 * before the first Python bean is instantiated.
 */
@Singleton
public class PythonRuntimeInitializer implements BeanCreatedEventListener<Object> {

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Override
    public Object onCreated(BeanCreatedEvent<Object> event) {
        if (initialized.compareAndSet(false, true)) {
            event.getSource().getBean(Context.class, Qualifiers.byName("python"));
        }
        return event.getBean();
    }
}
