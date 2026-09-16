from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


# TODO(python): see ProductListener, the queue group cannot be declared on a Python @Subject method yet
@Disabled("TODO(python): queue groups cannot be declared on a Python @Subject method")
@Property(name="spec.name", value="QueueSpec")
@MicronautTest(environments=["nats"])
class QueueSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_product_client_and_listener(self):
        # tag::producer[]
        self.product_client.send("queue-test".encode())
        # end::producer[]

        for _ in range(600):
            if self.product_listener.message_lengths == ["queue-test"]:
                break
            sleep(0.1)
        assert self.product_listener.message_lengths == ["queue-test"]
