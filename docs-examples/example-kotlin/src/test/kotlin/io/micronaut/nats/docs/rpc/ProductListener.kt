package io.micronaut.nats.docs.rpc

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "RpcUppercaseSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    @Subject("product")
    fun receive(data: String): String { // <1>
        return data.uppercase() // <2>
    }
}
// end::clazz[]
