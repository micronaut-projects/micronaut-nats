package io.micronaut.nats.rpc

import io.micronaut.context.annotation.Requires
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 *
 * @author jgrimm
 */
@Requires(property = "spec.name", value = "RpcSpec")
@NatsClient
interface Publisher {

    @Subject("rpc")
    Flowable<String> rpcCall(String data)

    @Subject("rpc")
    Maybe<String> rpcCallMaybe(String data)

    @Subject("rpc")
    Single<String> rpcCallSingle(String data)

    @Subject("rpc")
    String rpcBlocking(String data)
}
