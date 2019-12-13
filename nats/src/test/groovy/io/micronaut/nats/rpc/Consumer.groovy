package io.micronaut.nats.rpc

import io.micronaut.context.annotation.Requires
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject

import javax.annotation.Nullable

/**
 *
 * @author jgrimm
 */
@Requires(property = "spec.name", value = "RpcSpec")
@NatsListener
class Consumer {

    @Subject("rpc")
    String echo(@Nullable String data) {
        return data?.toUpperCase()
    }
}
