package com.github.grimmjo.micronaut.nats.bind;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.nats.client.Message;

/**
 * Used to determine which {@link NatsArgumentBinder} to use for any given argument
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Singleton
public class NatsBinderRegistry implements ArgumentBinderRegistry<Message> {

    private final Map<Class<? extends Annotation>, ArgumentBinder<?, Message>> byAnnotation = new LinkedHashMap<>();
    private final NatsDefaultBinder defaultBinder;

    /**
     * Default constructor.
     *
     * @param defaultBinder The binder to use when one cannot be found for an argument
     * @param binders The list of binders to choose from to bind the argument
     */
    public NatsBinderRegistry(NatsDefaultBinder defaultBinder, NatsArgumentBinder... binders) {
        this.defaultBinder = defaultBinder;
        if (ArrayUtils.isNotEmpty(binders)) {
            for (NatsArgumentBinder binder : binders) {
                if (binder instanceof NatsAnnotatedArgumentBinder) {
                    NatsAnnotatedArgumentBinder<?> annotatedBinder = (NatsAnnotatedArgumentBinder<?>) binder;
                    byAnnotation.put(
                            annotatedBinder.getAnnotationType(),
                            binder
                    );
                }
            }
        }
    }

    @Override
    public <T> Optional<ArgumentBinder<T, Message>> findArgumentBinder(Argument<T> argument, Message source) {
        Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(
                Bindable.class);
        if (opt.isPresent()) {
            Class<? extends Annotation> annotationType = opt.get();
            ArgumentBinder binder = byAnnotation.get(annotationType);
            if (binder != null) {
                return Optional.of(binder);
            }
        }
        return Optional.of((ArgumentBinder<T, Message>) defaultBinder);
    }
}
