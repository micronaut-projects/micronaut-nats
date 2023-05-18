package io.micronaut.nats.docs.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionError
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.nats.bind.NatsHeaderConvertibleValues
import io.micronaut.nats.bind.NatsTypeArgumentBinder
import io.nats.client.Message
import io.nats.client.impl.Headers

import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoTypeBinder implements NatsTypeArgumentBinder<ProductInfo> { //<2>

    private final ConversionService conversionService

    ProductInfoTypeBinder(ConversionService conversionService) { //<3>
        this.conversionService = conversionService
    }

    @Override
    Argument<ProductInfo> argumentType() {
        return Argument.of(ProductInfo)
    }

    @Override
    BindingResult<ProductInfo> bind(ArgumentConversionContext<ProductInfo> context, Message source) {
        Headers rawHeaders = source.headers //<4>

        if (rawHeaders == null) {
            return BindingResult.EMPTY
        }

        def headers = new NatsHeaderConvertibleValues(rawHeaders, conversionService)

        String size = headers.get("productSize", String).orElse(null)  //<5>
        Optional<Long> count = headers.get("x-product-count", Long) //<6>
        Optional<Boolean> sealed = headers.get("x-product-sealed", Boolean) // <7>

        if (headers.conversionErrors.isEmpty() && count.isPresent() && sealed.isPresent()) {
            { -> Optional.of(new ProductInfo(size, count.get(), sealed.get())) } //<8>
        } else {
            new BindingResult<ProductInfo>() {
                @Override
                Optional<ProductInfo> getValue() {
                    Optional.empty()
                }

                @Override
                List<ConversionError> getConversionErrors() {
                    headers.conversionErrors //<9>
                }
            }
        }
    }
}
// end::clazz[]
