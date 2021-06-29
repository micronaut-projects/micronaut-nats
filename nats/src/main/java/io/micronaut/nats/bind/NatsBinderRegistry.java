/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.bind;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.nats.client.Message;
import jakarta.inject.Singleton;

/**
 * Used to determine which {@link NatsArgumentBinder} to use for any given argument.
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
