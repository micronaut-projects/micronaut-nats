from abc import ABC, abstractmethod

from micronaut.context.annotation import Requires
from micronaut.nats.annotation import NatsClient, Subject


@Requires(property="spec.name", value="SIDSpec")
@NatsClient
class ProductClient(ABC):

    @Subject("product")
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...
