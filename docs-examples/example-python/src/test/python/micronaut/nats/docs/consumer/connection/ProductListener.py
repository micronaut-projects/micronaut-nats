from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
# end::imports[]


@Requires(property="spec.name", value="ConnectionSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.message_lengths: list[str] = []

    @Subject(value="product", connection="product-cluster")  # <1>
    def receive(self, data: bytes) -> None:
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")
# end::clazz[]
