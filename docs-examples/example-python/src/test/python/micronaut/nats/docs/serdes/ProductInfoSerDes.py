from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from jakarta.inject import Singleton
    from micronaut.core.convert import ConversionService
    from micronaut.core.type import Argument
    from micronaut.nats.serdes import NatsMessageSerDes
    from java.lang import Boolean, Long
    from .ProductInfo import ProductInfo
    from io.nats.client import Message
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import Message


@Requires(property="spec.name", value="ProductInfoSerDesSpec")
# tag::clazz[]
@Singleton  # <1>
class ProductInfoSerDes(NatsMessageSerDes):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def deserialize(self, message: Message, argument: Argument) -> ProductInfo | None:  # <4>
        body = bytes(message.getData()).decode("utf-8")
        parts = body.split("|")
        if len(parts) == 3:
            size = parts[0]
            if size == "null":
                size = None

            count = self.conversion_service.convert(parts[1], Long)
            sealed = self.conversion_service.convert(parts[2], Boolean)

            if count.isPresent() and sealed.isPresent():
                return ProductInfo(size, count.get(), sealed.get())
        return None

    # TODO(python): a `bytes | None` return type is coerced with Value.asByte(), so the method is declared to return `bytes`
    def serialize(self, data: ProductInfo) -> bytes:  # <5>
        if data is None:
            return None
        return f"{data.size if data.size is not None else 'null'}|{data.count}|{str(data.sealed).lower()}".encode("utf-8")

    def supports(self, argument: Argument) -> bool:  # <6>
        return argument.getType().isAssignableFrom(ProductInfo)
# end::clazz[]
