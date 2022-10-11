package io.micronaut.nats.docs.jetstream

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.nats.AbstractNatsTest
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JetstreamSpec : AbstractNatsTest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)
        val jsm = ctx.getBean(JetStreamManagement::class.java)


        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
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
                eventually(Duration.seconds(10)) {
                    productListener.messageLengths.size shouldBe 2
                    jsm.getStreamInfo("events").streamState.msgCount shouldBe 2
                }
            }
        }

        jsm.deleteStream("events")
        ctx.stop()
    }

    given("Pull consumer") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val pullConsumerHelper = ctx.getBean(PullConsumerHelper::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
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
                eventually(Duration.seconds(10)) {
                    pullConsumerHelper.pullMessages().size shouldBe 2
                }
            }
        }

        val jsm = ctx.getBean(JetStreamManagement::class.java)
        jsm.deleteStream("events")

        ctx.stop()
    }
})
