from abc import ABC, abstractmethod
from typing import Annotated

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.messaging.annotation import MessageBody
from micronaut.nats.annotation import NatsClient, Subject
from .ProductInfo import ProductInfo
# end::imports[]


@Requires(property="spec.name", value="ProductInfoSerDesSpec")
# tag::clazz[]
@NatsClient
class ProductClient(ABC):

    @Subject("product")
    @abstractmethod
    def send(self, data: Annotated[ProductInfo, MessageBody]) -> None:
        ...
# end::clazz[]
