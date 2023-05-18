package io.micronaut.nats.docs.rpc

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
// end::imports[]

@Requires(property = "spec.name", value = "RpcUppercaseSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    @Subject("product")
    String toUpperCase(String data) { // <1>
        data.toUpperCase() // <2>
    }
}
// end::clazz[]
