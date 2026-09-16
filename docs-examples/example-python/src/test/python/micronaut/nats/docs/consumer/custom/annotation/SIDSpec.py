from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="SIDSpec")
@MicronautTest(environments=["nats"])
class SIDSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_using_a_custom_annotation_binder(self):
        # tag::producer[]
        self.product_client.send("body".encode())
        self.product_client.send("body2".encode())
        self.product_client.send("body3".encode())
        # end::producer[]

        for _ in range(600):
            if len(self.product_listener.messages) == 3:
                break
            sleep(0.1)
        assert len(self.product_listener.messages) == 3
