package io.micronaut.nats.docs.rpc

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import org.reactivestreams.Publisher
// end::imports[]

@Requires(property = "spec.name", value = "RpcUppercaseSpec")
// tag::clazz[]
@NatsClient
interface ProductClient {

    @Subject("product")
    fun send(data: String): String // <1>

    @Subject("product")
    fun sendReactive(data: String): Publisher<String> // <2>
}
// end::clazz[]
