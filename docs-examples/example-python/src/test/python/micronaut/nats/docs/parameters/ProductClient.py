from abc import ABC, abstractmethod
from typing import Annotated

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsClient, Subject
# end::imports[]


@Requires(property="spec.name", value="BindingSpec")
# tag::clazz[]
@NatsClient
class ProductClient(ABC):

    @Subject("product")  # <1>
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @abstractmethod
    def send_to_subject(self, subject: Annotated[str, Subject], data: bytes) -> None:  # <2>
        ...
# end::clazz[]
