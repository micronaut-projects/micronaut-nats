from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
# end::imports[]


@Requires(property="spec.name", value="QuickstartSpec")
# tag::clazz[]
@NatsListener  # <1>
class ProductListener:

    def __init__(self):
        self.message_lengths: list[str] = []

    @Subject("product")  # <2>
    def receive(self, data: bytes) -> None:  # <3>
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from Nats")
# end::clazz[]
