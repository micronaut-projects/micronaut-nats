package io.micronaut.nats.docs.jetstream;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.jetstream.PullConsumerRegistry;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
// end::imports[]
import jakarta.inject.Singleton;

@Requires(property = "spec.name", value = "JetstreamTest")
// tag::clazz[]
@Singleton
public class PullConsumerHelper {

    private final PullConsumerRegistry pullConsumerRegistry;

    public PullConsumerHelper(PullConsumerRegistry pullConsumerRegistry) { // <1>
        this.pullConsumerRegistry = pullConsumerRegistry;
    }

    public List<Message> pullMessages() throws JetStreamApiException, IOException {
        PullSubscribeOptions pullSubscribeOptions =
            PullSubscribeOptions.builder()
                                .stream("events")
                                .configuration(
                                    ConsumerConfiguration
                                        .builder()
                                        .ackWait(
                                            Duration.ofMillis(
                                                2500))
                                        .build())
                                .build();
        JetStreamSubscription jetStreamSubscription =
            pullConsumerRegistry.newPullConsumer("events.>", pullSubscribeOptions); // <2>

        List<Message> messages = jetStreamSubscription.fetch(2, Duration.ofSeconds(2L)); // <3>
        messages.forEach(Message::ack); // <4>
        return messages;
    }
}
// end::clazz[]
