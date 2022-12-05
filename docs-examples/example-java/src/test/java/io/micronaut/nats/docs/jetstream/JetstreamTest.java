package io.micronaut.nats.docs.jetstream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.micronaut.nats.AbstractNatsTest;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import org.junit.jupiter.api.Test;

class JetstreamTest extends AbstractNatsTest {

    @Test
    void simplePublisher() throws JetStreamApiException, IOException {
        startContext();

        // tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
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

        ProductListener productListener = applicationContext.getBean(ProductListener.class);
        JetStreamManagement jsm = applicationContext.getBean(JetStreamManagement.class);

        waitFor(() ->
            productListener.messageLengths.size() == 2 &&
                jsm.getStreamInfo("events").getStreamState().getMsgCount() == 2
        );

        jsm.deleteStream("events");
    }

    @Test
    void pullConsumer() throws JetStreamApiException, IOException {
        startContext();

        ProductClient productClient = applicationContext.getBean(ProductClient.class);
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

        PullConsumerHelper pullConsumerHelper = applicationContext.getBean(PullConsumerHelper.class);

        waitFor(() ->
            pullConsumerHelper.pullMessages().size() == 2
        );

        JetStreamManagement jsm = applicationContext.getBean(JetStreamManagement.class);
        jsm.deleteStream("events");
    }
}
