package io.micronaut.nats.docs.jetstream.os

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreSpec")
class ObjectStoreSpec(holder: ObjectStoreHolder) : BehaviorSpec({

    given("A key value store holder") {
        `when`("A key is put into the store") {
            holder.put("test", "myvalue".byteInputStream())

            then("The key is stored with its value") {
                holder.store.getInfo("test") shouldNotBe null
            }
        }
    }

})
