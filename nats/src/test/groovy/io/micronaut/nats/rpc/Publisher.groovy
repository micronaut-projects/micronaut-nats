/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.rpc

import io.micronaut.context.annotation.Requires
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.Subject
import io.reactivex.rxjava3.core.Completable
import reactor.core.publisher.Mono

/**
 *
 * @author jgrimm
 */
@Requires(property = "spec.name", value = "RpcSpec")
@NatsClient
interface Publisher {

    @Subject("rpc")
    org.reactivestreams.Publisher<String> rpcCall(String data)

    @Subject("rpc")
    Completable rpcCallAsCompletable(String data)

    @Subject("rpc")
    org.reactivestreams.Publisher<Void> noRpcCall(String data)

    @Subject("rpc")
    Mono<Void> noRpcCallMono(String data)
}
