from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
# end::imports[]


@Requires(property="spec.name", value="BindingSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.message_lengths: list[int] = []

    @Subject("product")  # <1>
    def receive(self, data: bytes) -> None:
        self.message_lengths.append(len(data))
# end::clazz[]
