package io.micronaut.nats.docs.jetstream.kv

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
@Property(name = "spec.name", value = "KeyValueSpec")
class KeyValueSpec(holder: KeyValueStoreHolder) : BehaviorSpec({

    given("A key value store holder") {
        `when`("A key is put into the store") {
            holder.put("test", "myvalue")

            then("The key is stored with its value") {
                holder.store.get("test").valueAsString shouldBe "myvalue"
            }
        }
    }

})
