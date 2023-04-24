package io.micronaut.nats.docs.jetstream

import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import kotlin.time.Duration.Companion.seconds

@MicronautTest
@Property(name = "spec.name", value = "JetstreamSpec")
class JetstreamSpec(
    productClient: ProductClient,
    productListener: ProductListener,
    jsm: JetStreamManagement,
    pullConsumerHelper: PullConsumerHelper
) : BehaviorSpec({

    given("A basic producer and consumer") {
        `when`("The messages are published") {

            // tag::producer[]
            val pa = productClient.send(
                "events.one", "ghi".toByteArray(),
                PublishOptions.builder()
                    .stream("events")
                    .messageId("id00001")
                    .expectedStream("events")
                    .build()
            )
            productClient.send(
                "events.two", "jkl".toByteArray(),
                PublishOptions.builder()
                    .stream("events")
                    .messageId("id00002")
                    .expectedStream("events")
                    .expectedLastMsgId("id00001")
                    .expectedLastSequence(pa.seqno)
                    .build()
            )
            // end::producer[]

            then("The messages are received with pull consumer") {
                eventually(10.seconds) {
                    productListener.messageLengths.size shouldBe 2
                    jsm.getStreamInfo("events").streamState.msgCount shouldBe 2
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
                    .stream("events")
                    .messageId("id00001")
                    .expectedStream("events")
                    .build()
            )
            productClient.send(
                "events.four", "jkl".toByteArray(),
                PublishOptions.builder()
                    .stream("events")
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
})
