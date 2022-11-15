package io.micronaut.nats.consumer.custom.type

import io.micronaut.context.annotation.Requires
// tag::imports[]
import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionError
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.nats.bind.NatsHeaderConvertibleValues
import io.micronaut.nats.bind.NatsTypeArgumentBinder
import io.micronaut.nats.docs.consumer.custom.type.ProductInfo
import io.nats.client.Message
import jakarta.inject.Singleton
import java.util.Optional
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoTypeBinder constructor(private val conversionService: ConversionService) //<3>
    : NatsTypeArgumentBinder<ProductInfo> { // <2>

    override fun argumentType(): Argument<ProductInfo> {
        return Argument.of(ProductInfo::class.java)
    }

    override fun bind(context: ArgumentConversionContext<ProductInfo>, source: Message): BindingResult<ProductInfo> {
        val rawHeaders = source.headers ?: return BindingResult { Optional.empty<ProductInfo>() } //<4>

        val headers = NatsHeaderConvertibleValues(rawHeaders, conversionService)

        val size = headers.get("productSize", String::class.java).orElse(null)  //<5>
        val count = headers.get("x-product-count", Long::class.java) //<6>
        val sealed = headers.get("x-product-sealed", Boolean::class.java) // <7>

        if (headers.conversionErrors.isEmpty() && count.isPresent && sealed.isPresent) {
            return BindingResult<ProductInfo> { Optional.of(ProductInfo(size, count.get(), sealed.get())) } //<8>
        } else {
            return object : BindingResult<ProductInfo> {
                override fun getValue(): Optional<ProductInfo> {
                    return Optional.empty()
                }

                override fun getConversionErrors(): List<ConversionError> {
                    return headers.conversionErrors //<9>
                }
            }
        }
    }
}
// end::clazz[]
