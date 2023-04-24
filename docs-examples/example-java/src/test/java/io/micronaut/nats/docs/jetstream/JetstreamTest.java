package io.micronaut.nats.docs.jetstream;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.nats.client.JetStreamManagement;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "JetstreamTest")
class JetstreamTest {

    @Test
    void simplePublisher(ProductClient productClient, ProductListener productListener, JetStreamManagement jsm) {

        // tag::producer[]
        PublishAck pa = productClient.send("events.one", "ghi".getBytes(StandardCharsets.UTF_8),
            PublishOptions.builder()
                .stream("events")
                .messageId("id00001")
                .expectedStream("events")
                .build());
        productClient.send("events.two", "jkl".getBytes(StandardCharsets.UTF_8),
            PublishOptions.builder()
                .stream("events")
                .messageId("id00002")
                .expectedStream("events")
                .expectedLastMsgId("id00001")
                .expectedLastSequence(pa.getSeqno())
                .build());
        // end::producer[]


        await().atMost(60, SECONDS).until(() ->
            productListener.messageLengths.size() == 2 &&
                jsm.getStreamInfo("events").getStreamState().getMsgCount() == 2
        );
    }

    @Test
    void pullConsumer(ProductClient productClient, PullConsumerHelper pullConsumerHelper) {
        PublishAck pa = productClient.send("events.three", "ghi".getBytes(StandardCharsets.UTF_8),
            PublishOptions.builder()
                .stream("events")
                .messageId("id00001")
                .expectedStream("events")
                .build());
        productClient.send("events.four", "jkl".getBytes(StandardCharsets.UTF_8),
            PublishOptions.builder()
                .stream("events")
                .messageId("id00002")
                .expectedStream("events")
                .expectedLastMsgId("id00001")
                .expectedLastSequence(pa.getSeqno())
                .build());

        await().atMost(60, SECONDS).until(() ->
            pullConsumerHelper.pullMessages().size() == 2
        );
    }
}
