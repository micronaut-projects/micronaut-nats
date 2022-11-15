package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.core.bind.ArgumentBinder
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.nats.bind.NatsAnnotatedArgumentBinder
import io.nats.client.Message
import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@Singleton // <1>
class SIDAnnotationBinder(private val conversionService: ConversionService) // <3>
    : NatsAnnotatedArgumentBinder<SID> { // <2>

    override fun getAnnotationType(): Class<SID> {
        return SID::class.java
    }

    override fun bind(context: ArgumentConversionContext<Any>, source: Message): ArgumentBinder.BindingResult<Any> {
        val sid = source.sid // <4>
        return ArgumentBinder.BindingResult { conversionService.convert(sid, context) } // <5>
    }
}
// end::clazz[]
