package io.micronaut.nats.docs.quickstart

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import java.util.Collections
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@NatsListener // <1>
class ProductListener {

    val messageLengths: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Subject("product") // <2>
    fun receive(data: ByteArray) { // <3>
        val string = String(data)
        messageLengths.add(string)
        println("Kotlin received ${data.size} bytes from Nats: ${string}")
    }
}
// end::clazz[]
