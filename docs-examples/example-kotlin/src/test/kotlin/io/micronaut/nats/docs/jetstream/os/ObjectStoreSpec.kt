package io.micronaut.nats.docs.jetstream.os

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.nats.AbstractNatsTest

class ObjectStoreSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A key value store holder") {
        val ctx = startContext(specName)
        val holder = ctx.getBean(ObjectStoreHolder::class.java)

        `when`("A key is put into the store") {
            holder.put("test", "myvalue".byteInputStream())

            then("The key is stored with its value") {
                holder.store.getInfo("test") shouldNotBe null
            }
        }
    }

})
