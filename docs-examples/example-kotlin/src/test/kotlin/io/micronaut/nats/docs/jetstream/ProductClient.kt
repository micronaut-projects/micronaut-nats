package io.micronaut.nats.docs.jetstream

import io.micronaut.context.annotation.Requires

// tag::imports[]
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.nats.client.PublishOptions
import io.nats.client.api.PublishAck
// end::imports[]

@Requires(property = "spec.name", value = "JetstreamSpec")
// tag::clazz[]
@JetStreamClient
interface ProductClient {

    fun send(@Subject subject: String, @MessageBody data: ByteArray, publishOptions: PublishOptions): PublishAck // <1>
}
// end::clazz[]
