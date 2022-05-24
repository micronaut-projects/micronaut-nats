package io.micronaut.nats.docs.consumer.custom.annotation

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.docs.consumer.custom.type.ProductInfo
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "SIDSpec")
// tag::clazz[]
@NatsListener
class ProductListener {

    var messages: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Subject("product")
    fun receive(data: ByteArray, @SID sid: String)  {// <1>
        messages.add(sid)
    }

}
// end::clazz[]
