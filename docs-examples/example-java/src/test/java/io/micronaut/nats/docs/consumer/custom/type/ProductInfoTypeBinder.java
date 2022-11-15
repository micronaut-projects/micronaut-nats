package io.micronaut.nats.docs.consumer.custom.type;

import java.util.List;
import java.util.Optional;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.nats.bind.NatsHeaderConvertibleValues;
import io.micronaut.nats.bind.NatsTypeArgumentBinder;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import jakarta.inject.Singleton;
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@Singleton // <1>
public class ProductInfoTypeBinder implements NatsTypeArgumentBinder<ProductInfo> { //<2>

    private final ConversionService conversionService;

    ProductInfoTypeBinder(ConversionService conversionService) { //<3>
        this.conversionService = conversionService;
    }

    @Override
    public Argument<ProductInfo> argumentType() {
        return Argument.of(ProductInfo.class);
    }

    @Override
    public BindingResult<ProductInfo> bind(ArgumentConversionContext<ProductInfo> context, Message source) {
        Headers rawHeaders = source.getHeaders(); //<4>

        if (rawHeaders == null) {
            return BindingResult.EMPTY;
        }

        NatsHeaderConvertibleValues headers = new NatsHeaderConvertibleValues(rawHeaders, conversionService);

        String size = headers.get("productSize", String.class).orElse(null);  //<5>
        Optional<Long> count = headers.get("x-product-count", Long.class); //<6>
        Optional<Boolean> sealed = headers.get("x-product-sealed", Boolean.class); // <7>

        if (headers.getConversionErrors().isEmpty() && count.isPresent() && sealed.isPresent()) {
            return () -> Optional.of(new ProductInfo(size, count.get(), sealed.get())); //<8>
        } else {
            return new BindingResult<ProductInfo>() {
                @Override
                public Optional<ProductInfo> getValue() {
                    return Optional.empty();
                }

                @Override
                public List<ConversionError> getConversionErrors() {
                    return headers.getConversionErrors(); //<9>
                }
            };
        }
    }
}
// end::clazz[]
