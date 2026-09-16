from typing import Annotated

from java.lang import Long
from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from micronaut.messaging.annotation import MessageBody, MessageHeader
    from micronaut.nats.annotation import NatsListener, Subject
    from io.nats.client.impl import Headers
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client.impl import Headers


@Requires(property="spec.name", value="HeadersSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.message_properties: set[str] = set()

    @Subject("product")
    def receive(self, data: bytes,
                sealed: Annotated[bool, MessageHeader("x-product-sealed")],  # <1>
                count: Annotated[Long, MessageHeader("x-product-count")],  # <2>
                product_size: Annotated[str | None, MessageHeader("productSize")]) -> None:  # <3>
        self.message_properties.add(f"{str(sealed).lower()}|{count}|{product_size if product_size is not None else 'null'}")

    @Subject("products")
    def receive_product_sizes(self, data: Annotated[bytes, MessageBody],
                              sealed: Annotated[bool, MessageHeader("x-product-sealed")],
                              count: Annotated[Long, MessageHeader("x-product-count")],
                              product_sizes: Annotated[list[str], MessageHeader("productSizes")]) -> None:  # <4>
        for product_size in product_sizes:
            self.message_properties.add(f"{str(sealed).lower()}|{count}|{product_size}")

    @Subject("productHeader")
    def receive_headers(self, data: Annotated[bytes, MessageBody], headers: Headers) -> None:  # <5>
        product_size = headers.get("productSize").get(0)
        self.message_properties.add(
            f"{headers.get('x-product-sealed').get(0)}|{headers.get('x-product-count').get(0)}|{product_size}")
# end::clazz[]
