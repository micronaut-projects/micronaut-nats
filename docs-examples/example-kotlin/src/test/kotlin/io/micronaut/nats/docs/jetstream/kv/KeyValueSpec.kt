package io.micronaut.nats.docs.jetstream.kv

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance

@MicronautTest
@Property(name = "spec.name", value = "KeyValueSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyValueSpec : BehaviorSpec(), TestPropertyProvider {

    @Inject
    lateinit var holder: KeyValueStoreHolder

    init {
        given("A key value store holder") {
            `when`("A key is put into the store") {
                holder.put("test", "myvalue")

                then("The key is stored with its value") {
                    holder.store.get("test").valueAsString shouldBe "myvalue"
                }
            }
        }
    }

    override fun getProperties(): Map<String, String> = Nats.getProperties()
}
