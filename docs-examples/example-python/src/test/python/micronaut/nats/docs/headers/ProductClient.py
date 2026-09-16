from abc import ABC, abstractmethod
from typing import Annotated

from java.lang import Long
from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from micronaut.messaging.annotation import MessageBody, MessageHeader
    from micronaut.nats.annotation import NatsClient, Subject
    from io.nats.client.impl import Headers
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client.impl import Headers


@Requires(property="spec.name", value="HeadersSpec")
# tag::clazz[]
@NatsClient
@MessageHeader(name="x-product-sealed", value="true")  # <1>
@MessageHeader(name="productSize", value="large")
class ProductClient(ABC):

    @Subject("product")
    @MessageHeader(name="x-product-count", value="10")  # <2>
    @MessageHeader(name="productSize", value="small")
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @Subject("product")
    @abstractmethod
    def send_with_headers(self, product_size: Annotated[str | None, MessageHeader("productSize")],  # <3>
                          count: Annotated[Long, MessageHeader("x-product-count")],
                          data: bytes) -> None:
        ...

    @Subject("products")
    @MessageHeader(name="x-product-count", value="20")
    @abstractmethod
    def send_with_product_sizes(self, data: Annotated[bytes, MessageBody],
                                product_sizes: Annotated[list[str], MessageHeader("productSizes")]) -> None:  # <4>
        ...

    @Subject("productHeader")
    @abstractmethod
    def send_with_nats_headers(self, data: Annotated[bytes, MessageBody], headers: Headers) -> None:  # <5>
        ...
# end::clazz[]
