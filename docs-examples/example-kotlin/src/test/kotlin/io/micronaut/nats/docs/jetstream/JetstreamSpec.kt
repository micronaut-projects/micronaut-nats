package io.micronaut.nats.docs.jetstream

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "JetstreamSpec")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JetstreamSpec : BehaviorSpec(), TestPropertyProvider {

    @Inject
    lateinit var productClient: ProductClient

    @Inject
    lateinit var productListener: ProductListener

    @Inject
    lateinit var jsm: JetStreamManagement

    @Inject
    lateinit var pullConsumerHelper: PullConsumerHelper

    init {
        given("A basic producer and consumer") {
            `when`("The messages are published") {

                // tag::producer[]
                val pa = productClient.send(
                    "myevents.one", "ghi".toByteArray(),
                    PublishOptions.builder()
                        .messageId("id00001")
                        .expectedStream("myevents")
                        .build()
                )
                productClient.send(
                    "myevents.two", "jkl".toByteArray(),
                    PublishOptions.builder()
                        .messageId("id00002")
                        .expectedStream("myevents")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.seqno)
                        .build()
                )
                // end::producer[]

                then("The messages are received with pull consumer") {
                    eventually(10.seconds) {
                        productListener.messageLengths.size shouldBe 2
                        jsm.getStreamInfo("myevents").streamState.msgCount shouldBe 2
                    }
                }
            }
        }

        given("Pull consumer") {
            `when`("The messages are published") {
                // tag::producer[]
                val pa = productClient.send(
                    "events.three", "ghi".toByteArray(),
                    PublishOptions.builder()
                        .messageId("id00001")
                        .expectedStream("events")
                        .build()
                )
                productClient.send(
                    "events.four", "jkl".toByteArray(),
                    PublishOptions.builder()
                        .messageId("id00002")
                        .expectedStream("events")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.seqno)
                        .build()
                )
                // end::producer[]

                then("The messages are received") {
                    eventually(10.seconds) {
                        pullConsumerHelper.pullMessages().size shouldBe 2
                    }
                }
            }
        }
    }

    override fun getProperties(): Map<String, String> = Nats.getProperties()
}
