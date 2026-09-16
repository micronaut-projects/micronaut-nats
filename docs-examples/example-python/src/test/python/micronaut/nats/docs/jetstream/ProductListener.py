from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.jetstream.annotation import JetStreamListener, PushConsumer
# end::imports[]


@Requires(property="spec.name", value="JetstreamTest")
# tag::clazz[]
@JetStreamListener  # <1>
class ProductListener:

    def __init__(self):
        self.message_lengths: list[bytes] = []

    @PushConsumer(value="myevents", subject="myevents.>", durable="test")  # <2>
    def receive(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data))
# end::clazz[]
