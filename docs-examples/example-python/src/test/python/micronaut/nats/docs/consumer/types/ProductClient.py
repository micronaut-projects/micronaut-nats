from abc import ABC, abstractmethod
from typing import Annotated

from java.lang import Long
from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.messaging.annotation import MessageHeader
from micronaut.nats.annotation import NatsClient, Subject
# end::imports[]


@Requires(property="spec.name", value="TypeBindingSpec")
# tag::clazz[]
@NatsClient  # <1>
class ProductClient(ABC):

    @Subject("product")  # <2>
    @abstractmethod
    def send(self, data: bytes, count: Annotated[Long, MessageHeader("x-product-count")]) -> None:  # <3>
        ...
# end::clazz[]
