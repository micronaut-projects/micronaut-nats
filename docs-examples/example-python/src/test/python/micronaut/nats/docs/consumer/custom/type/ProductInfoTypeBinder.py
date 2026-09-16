from micronaut.context.annotation import Requires
try:
    # tag::imports[]
    from jakarta.inject import Singleton
    from micronaut.core.bind import ArgumentBinder
    from micronaut.core.convert import ArgumentConversionContext, ConversionService
    from micronaut.core.type import Argument
    from micronaut.nats.bind import NatsHeaderConvertibleValues, NatsTypeArgumentBinder
    from java.lang import Boolean, Long, String
    from java.util import Optional
    from .ProductInfo import ProductInfo
    from io.nats.client import Message
    # end::imports[]
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import Message


@Requires(property="spec.name", value="ProductInfoSpec")
# tag::clazz[]
@Singleton  # <1>
class ProductInfoTypeBinder(NatsTypeArgumentBinder):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def argumentType(self):
        return Argument.of(ProductInfo)

    def bind(self, context: ArgumentConversionContext, source: Message):
        raw_headers = source.getHeaders()  # <4>

        if raw_headers is None:
            return ArgumentBinder.BindingResult.empty()

        headers = NatsHeaderConvertibleValues(raw_headers, self.conversion_service)

        size = headers.get("productSize", String).orElse(None)  # <5>
        count = headers.get("x-product-count", Long)  # <6>
        sealed = headers.get("x-product-sealed", Boolean)  # <7>

        if headers.getConversionErrors().isEmpty() and count.isPresent() and sealed.isPresent():
            return lambda: Optional.of(ProductInfo(size, count.get(), sealed.get()))  # <8>
        else:
            class ConversionErrorsBindingResult:
                def getValue(self):
                    return Optional.empty()

                def getConversionErrors(self):
                    return headers.getConversionErrors()  # <9>

            return ConversionErrorsBindingResult()
# end::clazz[]
