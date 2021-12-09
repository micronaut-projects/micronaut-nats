package io.micronaut.nats.docs.consumer.custom.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.micronaut.context.annotation.Requires;
// tag::imports[]
import io.micronaut.nats.annotation.NatsListener;
import io.micronaut.nats.annotation.Subject;
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@NatsListener
public class ProductListener {

    List<ProductInfo> messages = Collections.synchronizedList(new ArrayList<>());

    @Subject("product")
    public void receive(byte[] data,
                        ProductInfo productInfo) { // <1>
        messages.add(productInfo);
    }
}
// end::clazz[]
