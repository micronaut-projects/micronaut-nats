package io.micronaut.nats.docs.jetstream

import io.micronaut.context.annotation.Property
import io.micronaut.nats.testcontainers.Nats
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import io.nats.client.api.PublishAck
import jakarta.inject.Inject
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.await

@MicronautTest
@Property(name = "spec.name", value = "JetstreamSpec")
class JetstreamSpec extends Specification implements TestPropertyProvider{
    @Inject ProductClient productClient
    @Inject ProductListener productListener
    @Inject JetStreamManagement jsm
    @Inject PullConsumerHelper pullConsumerHelper


    void "simple producer"() {

        when:
// tag::producer[]
        PublishAck pa = productClient.send("myevents.one", "ghi".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .messageId("id00001")
                        .expectedStream("myevents")
                        .build())
        productClient.send("myevents.two", "jkl".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .messageId("id00002")
                        .expectedStream("myevents")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.getSeqno())
                        .build())
// end::producer[]

        then:
        await().atMost(10, SECONDS).until {
            productListener.messageLengths.size() == 2
            jsm.getStreamInfo("myevents").getStreamState().getMsgCount() == 2
        }
    }

    void "pull consumer"() {

        when:
        PublishAck pa = productClient.send("events.three", "ghi".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .messageId("id00001")
                        .expectedStream("events")
                        .build())
        productClient.send("events.four", "jkl".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .messageId("id00002")
                        .expectedStream("events")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.getSeqno())
                        .build())

        then:
        await().atMost(10, SECONDS).until {
            pullConsumerHelper.pullMessages().size() == 2
        }

    }

    @Override
    Map<String, String> getProperties() {
        Nats.properties
    }
}
