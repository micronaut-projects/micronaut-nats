from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductInfo import ProductInfo
from .ProductListener import ProductListener


@Property(name="spec.name", value="ProductInfoSerDesSpec")
@MicronautTest(environments=["nats"])
class ProductInfoSerDesSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_using_a_custom_serdes(self):
        # tag::producer[]
        self.product_client.send(ProductInfo("small", 10, True))
        self.product_client.send(ProductInfo("medium", 20, True))
        self.product_client.send(ProductInfo(None, 30, False))
        # end::producer[]

        for _ in range(600):
            if len(self.product_listener.messages) == 3:
                break
            sleep(0.1)
        counts = sorted(int(pi.count) for pi in self.product_listener.messages)
        assert counts == [10, 20, 30]
