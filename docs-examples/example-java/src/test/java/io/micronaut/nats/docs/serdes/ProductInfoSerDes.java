package io.micronaut.nats.docs.serdes;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.nats.serdes.NatsMessageSerDes;
import io.nats.client.Message;
import jakarta.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@Singleton // <1>
public class ProductInfoSerDes implements NatsMessageSerDes<ProductInfo> { // <2>

    private final ConversionService conversionService;

    public ProductInfoSerDes(ConversionService conversionService) { // <3>
        this.conversionService = conversionService;
    }

    @Override
    public ProductInfo deserialize(Message message, Argument<ProductInfo> argument) { // <4>
        String body = new String(message.getData(), StandardCharsets.UTF_8);
        String[] parts = body.split("\\|");
        if (parts.length == 3) {
            String size = parts[0];
            if (size.equals("null")) {
                size = null;
            }

            Optional<Long> count = conversionService.convert(parts[1], Long.class);
            Optional<Boolean> sealed = conversionService.convert(parts[2], Boolean.class);

            if (count.isPresent() && sealed.isPresent()) {
                return new ProductInfo(size, count.get(), sealed.get());
            }
        }
        return null;
    }

    @Override
    public byte[] serialize(ProductInfo data) { // <5>
        if (data == null) {
            return null;
        }
        return (data.getSize() + "|" + data.getCount() + "|" + data.getSealed()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean supports(Argument<ProductInfo> argument) { // <6>
        return argument.getType().isAssignableFrom(ProductInfo.class);
    }
}
// end::clazz[]
