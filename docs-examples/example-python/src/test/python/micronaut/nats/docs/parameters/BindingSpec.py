from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="BindingSpec")
@MicronautTest(environments=["nats"])
class BindingSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_dynamic_binding(self):
        # tag::producer[]
        self.product_client.send("message body".encode())
        self.product_client.send_to_subject("product", "message body2".encode())
        # end::producer[]

        for _ in range(600):
            if len(self.product_listener.message_lengths) == 2:
                break
            sleep(0.1)
        assert sorted(self.product_listener.message_lengths) == [12, 13]
