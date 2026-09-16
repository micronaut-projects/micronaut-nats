from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from micronaut.nats.jetstream import PullConsumerRegistry
    from java.time import Duration
    from io.nats.client import JetStreamSubscription, Message
    from io.nats.client.api import ConsumerConfiguration
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import JetStreamSubscription, Message
    from nats.client.api import ConsumerConfiguration
from jakarta.inject import Singleton


@Requires(property="spec.name", value="JetstreamTest")
# tag::clazz[]
@Singleton
class PullConsumerHelper:

    def __init__(self, pull_consumer_registry: PullConsumerRegistry):  # <1>
        self.pull_consumer_registry = pull_consumer_registry

    def pull_messages(self) -> list[Message]:
        # TODO(python): the methods PullSubscribeOptions.Builder inherits from the generic SubscribeOptions.Builder
        # (stream(...), configuration(...)) are not exposed to GraalPy, so the options are built from the consumer
        # configuration and the stream is resolved from the subject.
        pull_subscribe_options = (
            ConsumerConfiguration.builder()
            .ackWait(Duration.ofMillis(2500))
            .buildPullSubscribeOptions())
        jet_stream_subscription: JetStreamSubscription = \
            self.pull_consumer_registry.newPullConsumer("events.>", pull_subscribe_options)  # <2>

        messages = jet_stream_subscription.fetch(2, Duration.ofSeconds(2))  # <3>
        for message in messages:
            message.ack()  # <4>
        return messages
# end::clazz[]
