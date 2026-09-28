from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.nats.annotation import NatsListener, Subject
from .ProductInfo import ProductInfo
# end::imports[]


@Requires(property="spec.name", value="ProductInfoSpec")
# tag::clazz[]
@NatsListener
class ProductListener:

    def __init__(self):
        self.messages: list[ProductInfo] = []

    @Subject("product")
    def receive(self, data: bytes, product_info: ProductInfo) -> None:  # <1>
        self.messages.append(product_info)
# end::clazz[]
