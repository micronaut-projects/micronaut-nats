from abc import ABC, abstractmethod
from typing import Annotated

from java.lang import Long
from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.messaging.annotation import MessageHeader
from micronaut.nats.annotation import NatsClient, Subject
# end::imports[]


@Requires(property="spec.name", value="ProductInfoSpec")
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
# end::clazz[]
