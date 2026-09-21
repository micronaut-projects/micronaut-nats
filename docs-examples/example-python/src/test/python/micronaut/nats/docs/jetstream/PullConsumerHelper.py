from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.jetstream import PullConsumerRegistry
from java.time import Duration
from io.nats.client import JetStreamSubscription, Message, PullSubscribeOptions
from io.nats.client.api import ConsumerConfiguration
# end::imports[]
from jakarta.inject import Singleton


@Requires(property="spec.name", value="JetstreamTest")
# tag::clazz[]
@Singleton
class PullConsumerHelper:

    def __init__(self, pull_consumer_registry: PullConsumerRegistry):  # <1>
        self.pull_consumer_registry = pull_consumer_registry

    def pull_messages(self) -> list[Message]:
        pull_subscribe_options = (
            PullSubscribeOptions.builder()
            .stream("events")
            .configuration(
                ConsumerConfiguration.builder()
                .ackWait(Duration.ofMillis(2500))
                .build())
            .build())
        jet_stream_subscription: JetStreamSubscription = \
            self.pull_consumer_registry.newPullConsumer("events.>", pull_subscribe_options)  # <2>

        messages = jet_stream_subscription.fetch(2, Duration.ofSeconds(2))  # <3>
        for message in messages:
            message.ack()  # <4>
        return messages
# end::clazz[]
