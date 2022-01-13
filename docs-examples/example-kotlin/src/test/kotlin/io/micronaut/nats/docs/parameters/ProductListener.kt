package io.micronaut.nats.docs.parameters

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    val messageLengths: MutableList<Int> = Collections.synchronizedList(ArrayList())

    @Subject("product") // <1>
    fun receive(data: ByteArray) {
        messageLengths.add(data.size)
    }
}
// end::clazz[]
