from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
try:
    from io.nats.client.impl import Headers
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client.impl import Headers

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="HeadersSpec")
@MicronautTest(environments=["nats"])
class HeadersSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_publishing_and_receiving_headers(self):
        # tag::producer[]
        self.product_client.send("body".encode())
        self.product_client.send_with_headers("medium", 20, "body2".encode())
        self.product_client.send_with_headers(None, 30, "body3".encode())

        headers = Headers()
        headers.put("productSize", "large")
        headers.put("x-product-count", "40")
        self.product_client.send_with_nats_headers("body4".encode(), headers)
        self.product_client.send_with_product_sizes("body5".encode(), ["xtra-small", "xtra-large"])
        # end::producer[]

        expected = {"true|10|small", "true|20|medium", "true|30|null", "true|40|large", "true|20|xtra-small", "true|20|xtra-large"}
        for _ in range(600):
            if self.product_listener.message_properties == expected:
                break
            sleep(0.1)
        assert self.product_listener.message_properties == expected
