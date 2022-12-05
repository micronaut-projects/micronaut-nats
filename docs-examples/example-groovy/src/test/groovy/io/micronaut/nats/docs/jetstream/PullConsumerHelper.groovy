package io.micronaut.nats.docs.jetstream

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.nats.jetstream.PullConsumerRegistry
import io.nats.client.JetStreamApiException
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
class PullConsumerHelper {

    private final PullConsumerRegistry pullConsumerRegistry;

    PullConsumerHelper(PullConsumerRegistry pullConsumerRegistry) {  // <1>
        this.pullConsumerRegistry = pullConsumerRegistry
    }

    List<Message> pullMessages() throws JetStreamApiException, IOException {
        JetStreamSubscription jetStreamSubscription =
                pullConsumerRegistry.newPullConsumer("events.>",
                        PullSubscribeOptions.builder()
                                .stream("events")
                                .configuration(
                                        ConsumerConfiguration.builder().ackWait(Duration.ofMillis(2500)).build())
                                .build()) // <2>

        List<Message> messages = jetStreamSubscription.fetch(2, Duration.ofSeconds(2L)) // <3>
        messages.forEach(Message::ack) // <4>
        return messages
    }

}
// end::clazz[]
