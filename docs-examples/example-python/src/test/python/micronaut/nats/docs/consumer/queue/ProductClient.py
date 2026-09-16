from abc import ABC, abstractmethod

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsClient, Subject
# end::imports[]


@Requires(property="spec.name", value="QueueSpec")
# tag::clazz[]
@NatsClient
class ProductClient(ABC):

    @Subject(value="product")  # <1>
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...
# end::clazz[]
