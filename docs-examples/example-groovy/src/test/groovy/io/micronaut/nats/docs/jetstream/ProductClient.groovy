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

    PublishAck send(@Subject String subject, @MessageBody byte[] data, PublishOptions publishOptions); // <2>
}
// end::clazz[]
