package io.micronaut.nats.docs.serdes

import io.micronaut.context.annotation.Requires
import java.nio.charset.Charset

// tag::imports[]
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.nats.serdes.NatsMessageSerDes
import io.nats.client.Message

import jakarta.inject.Singleton
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoSerDes implements NatsMessageSerDes<ProductInfo> { // <2>

    private static final Charset UTF8 = Charset.forName("UTF-8")

    private final ConversionService conversionService

    ProductInfoSerDes(ConversionService conversionService) { // <3>
        this.conversionService = conversionService
    }

    @Override
    ProductInfo deserialize(Message message, Argument<ProductInfo> argument) { // <4>
        String body = new String(message.data, UTF8)
        String[] parts = body.split("\\|")
        if (parts.length == 3) {
            String size = parts[0]
            if (size == "null") {
                size = null
            }

            Optional<Long> count = conversionService.convert(parts[1], Long)
            Optional<Boolean> sealed = conversionService.convert(parts[2], Boolean)

            if (count.isPresent() && sealed.isPresent()) {
                return new ProductInfo(size, count.get(), sealed.get())
            }
        }
        null
    }

    @Override
    byte[] serialize(ProductInfo data) { // <5>
        if (data == null) {
            return null
        }
        (data.size + "|" + data.count + "|" + data.sealed).getBytes(UTF8)
    }

    @Override
    boolean supports(Argument<ProductInfo> argument) { // <6>
        argument.type.isAssignableFrom(ProductInfo)
    }
}
// end::clazz[]
