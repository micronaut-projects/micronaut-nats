package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.nats.bind.NatsAnnotatedArgumentBinder
import io.nats.client.Message

import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@Singleton // <1>
class SIDAnnotationBinder implements NatsAnnotatedArgumentBinder<SID> { // <2>

    private final ConversionService conversionService

    SIDAnnotationBinder(ConversionService conversionService) { // <3>
        this.conversionService = conversionService
    }

    @Override
    Class<SID> getAnnotationType() {
        SID
    }

    @Override
    BindingResult<Object> bind(ArgumentConversionContext<Object> context, Message source) {
        String sid = source.getSID() // <4>
        return { -> conversionService.convert(sid, context) } // <5>
    }
}
// end::clazz[]
