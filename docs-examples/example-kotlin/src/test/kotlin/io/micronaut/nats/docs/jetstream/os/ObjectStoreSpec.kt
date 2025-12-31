package io.micronaut.nats.docs.jetstream.os

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance

@MicronautTest
@Property(name = "spec.name", value = "ObjectStoreSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreSpec : BehaviorSpec(), TestPropertyProvider {

    @Inject
    lateinit var holder: ObjectStoreHolder

    init {
        given("An object store holder") {
            `when`("An object is put into the store") {
                holder.put("test", "myvalue".byteInputStream())

                then("The object info is retrievable") {
                    holder.store.getInfo("test") shouldNotBe null
                }
            }
        }
    }

    override fun getProperties(): Map<String, String> = Nats.getProperties()
}
