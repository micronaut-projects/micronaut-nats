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

    # TODO(python): `queue="product-queue"` cannot be set on the @Subject of a Python method yet: the member
    # aliases @NatsListener.queue, whose @MessageListener stereotype makes the Python compiler treat the
    # method as a @Bean factory method ("Factory methods declared with @Bean must specify a return type").
    @Subject(value="product")  # <1>
    def receive_by_queue1(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")

    @Subject(value="product")
    def receive_by_queue2(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")
# end::clazz[]
