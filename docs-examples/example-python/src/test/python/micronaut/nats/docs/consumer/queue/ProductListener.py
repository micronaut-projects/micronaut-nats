from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
# end::imports[]


@Requires(property="spec.name", value="QueueSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.message_lengths: list[str] = []

    @Subject(value="product", queue="product-queue")  # <1>
    def receive_by_queue1(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")

    @Subject(value="product", queue="product-queue")
    def receive_by_queue2(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")
# end::clazz[]
