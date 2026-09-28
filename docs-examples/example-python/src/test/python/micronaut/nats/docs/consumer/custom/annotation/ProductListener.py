from typing import Annotated

from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
from .SID import SID
# end::imports[]


@Requires(property="spec.name", value="SIDSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.messages: list[str] = []

    @Subject("product")
    def receive(self, data: bytes, sid: Annotated[str, SID]) -> None:  # <1>
        self.messages.append(sid)
# end::clazz[]
