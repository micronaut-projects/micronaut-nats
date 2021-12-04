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

import java.util.Optional;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.messaging.annotation.MessageHeader;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import jakarta.inject.Singleton;

/**
 * Binds an argument of with the {@link MessageHeader} annotation from the {@link Message}.
 *
 * @author Joachim Grimm
 * @since 3.1.0
 */
@Singleton
public class NatsHeaderBinder implements NatsAnnotatedArgumentBinder<MessageHeader> {

    private final ConversionService<?> conversionService;

    /**
     * Default constructor.
     * @param conversionService The conversation service
     */
    public NatsHeaderBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<MessageHeader> getAnnotationType() {
        return MessageHeader.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, Message messageState) {
        String parameterName = context.getAnnotationMetadata()
                                      .getValue(MessageHeader.class, String.class)
                                      .orElse(context.getArgument().getName());

        Headers rawHeaders = messageState.getHeaders();
        NatsHeaderConvertibleValues headers = new NatsHeaderConvertibleValues(rawHeaders, conversionService);
        Optional<Object> header = headers.get(parameterName, context);
        return () -> header;
    }
}
