from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="TypeBindingSpec")
@MicronautTest(environments=["nats"])
class TypeBindingSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_binding_by_type(self):
        # tag::producer[]
        self.product_client.send("body".encode(), 20)
        self.product_client.send("body2".encode(), 30)
        self.product_client.send("body3".encode(), 40)
        # end::producer[]

        expected = [
            "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [20]",
            "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [30]",
            "subject: [product], maxPayload: [1048576], pendingMessageCount: [0], x-productCount: [40]",
        ]
        for _ in range(600):
            if len(self.product_listener.messages) == 3:
                break
            sleep(0.1)
        assert len(self.product_listener.messages) == 3
        for message in expected:
            assert message in self.product_listener.messages
