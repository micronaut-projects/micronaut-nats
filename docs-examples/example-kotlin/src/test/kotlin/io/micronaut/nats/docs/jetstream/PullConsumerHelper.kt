package io.micronaut.nats.docs.jetstream


import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.nats.jetstream.PullConsumerRegistry
import io.nats.client.JetStreamSubscription
import io.nats.client.Message
import io.nats.client.PullSubscribeOptions
import io.nats.client.api.ConsumerConfiguration
// end::imports[]
import jakarta.inject.Singleton
import java.time.Duration



@Requires(property = "spec.name", value = "JetstreamSpec")
// tag::clazz[]
@Singleton
class PullConsumerHelper(private val pullConsumerRegistry: PullConsumerRegistry) { // <1>

    fun pullMessages(): List<Message> {
        val pullSubscribeOptions: PullSubscribeOptions =
            PullSubscribeOptions.builder()
                .stream("events")
                .configuration(
                    ConsumerConfiguration
                        .builder()
                        .ackWait(
                            Duration.ofMillis(
                                2500
                            )
                        )
                        .build()
                )
                .build()
        val jetStreamSubscription: JetStreamSubscription =
            pullConsumerRegistry.newPullConsumer("events.>", pullSubscribeOptions) // <2>

        val messages: List<Message> = jetStreamSubscription.fetch(2, Duration.ofSeconds(2L)) // <3>
        messages.forEach(Message::ack) // <4>
        return messages
    }

}
// end::clazz[]
