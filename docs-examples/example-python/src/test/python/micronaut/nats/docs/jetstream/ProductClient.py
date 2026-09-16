from abc import ABC, abstractmethod
from typing import Annotated

from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from micronaut.messaging.annotation import MessageBody
    from micronaut.nats.annotation import Subject
    from micronaut.nats.jetstream.annotation import JetStreamClient
    from io.nats.client import PublishOptions
    from io.nats.client.api import PublishAck
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import PublishOptions
    from nats.client.api import PublishAck


@Requires(property="spec.name", value="JetstreamTest")
# tag::clazz[]
@JetStreamClient
class ProductClient(ABC):

    @abstractmethod
    def send(self, subject: Annotated[str, Subject], data: Annotated[bytes, MessageBody], publish_options: PublishOptions) -> PublishAck:  # <1>
        ...
# end::clazz[]
