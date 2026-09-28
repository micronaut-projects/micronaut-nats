from abc import ABC, abstractmethod

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsClient, Subject
# end::imports[]


@Requires(property="spec.name", value="QuickstartSpec")
# tag::clazz[]
@NatsClient  # <1>
class ProductClient(ABC):

    @Subject("product")  # <2>
    @abstractmethod
    def send(self, data: bytes) -> None:  # <3>
        ...
# end::clazz[]
