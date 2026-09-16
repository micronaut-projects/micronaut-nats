from abc import ABC, abstractmethod

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsClient, Subject
from org.reactivestreams import Publisher
# end::imports[]


@Requires(property="spec.name", value="RpcUppercaseSpec")
# tag::clazz[]
@NatsClient
class ProductClient(ABC):

    @Subject("product")
    @abstractmethod
    def send(self, data: str) -> str:  # <1>
        ...

    @Subject("product")
    @abstractmethod
    def send_reactive(self, data: str) -> Publisher[str]:  # <2>
        ...
# end::clazz[]
