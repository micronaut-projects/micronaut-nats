package io.micronaut.nats.docs.jetstream.kv

import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest

class KeyValueSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A key value store holder") {
        val ctx = startContext(specName)
        val holder = ctx.getBean(KeyValueStoreHolder::class.java)

        `when`("A key is put into the store") {
            holder.put("test", "myvalue")

            then("The key is stored with its value") {
                holder.store.get("test").valueAsString shouldBe "myvalue"
            }
        }
    }

})
