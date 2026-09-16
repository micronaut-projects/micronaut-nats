from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from micronaut.nats.annotation import NatsListener, Subject
    from io.nats.client import Connection, Message, Subscription
    from io.nats.client.impl import Headers
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import Connection, Message, Subscription
    from nats.client.impl import Headers


@Requires(property="spec.name", value="TypeBindingSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.messages: list[str] = []

    @Subject("product")
    def receive(self, data: bytes, message: Message, connection: Connection, subscription: Subscription, headers: Headers) -> None:  # <1>
        self.messages.append(
            f"subject: [{message.getSubject()}], maxPayload: [{connection.getMaxPayload()}], "
            f"pendingMessageCount: [{subscription.getPendingMessageCount()}], x-productCount: [{headers.get('x-product-count').get(0)}]"
        )
# end::clazz[]
