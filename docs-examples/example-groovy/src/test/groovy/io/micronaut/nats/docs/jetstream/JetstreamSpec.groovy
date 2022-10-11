package io.micronaut.nats.docs.jetstream

import io.micronaut.nats.AbstractNatsTest
import io.nats.client.JetStreamManagement
import io.nats.client.PublishOptions
import io.nats.client.api.PublishAck

import java.nio.charset.StandardCharsets

class JetstreamSpec extends AbstractNatsTest {

    void "simple producer"() {
        startContext()

        when:
// tag::producer[]
        def productClient = applicationContext.getBean(ProductClient)
        PublishAck pa = productClient.send("events.one", "ghi".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .stream("events")
                        .messageId("id00001")
                        .expectedStream("events")
                        .build())
        productClient.send("events.two", "jkl".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .stream("events")
                        .messageId("id00002")
                        .expectedStream("events")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.getSeqno())
                        .build())
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)
        JetStreamManagement jsm = applicationContext.getBean(JetStreamManagement)

        then:
        waitFor {
            productListener.messageLengths.size() == 2
            jsm.getStreamInfo("events").getStreamState().getMsgCount() == 2
        }

        cleanup:
        jsm.deleteStream("events")
    }

    void "pull consumer"() {
        startContext()

        when:
        def productClient = applicationContext.getBean(ProductClient)
        PublishAck pa = productClient.send("events.three", "ghi".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .stream("events")
                        .messageId("id00001")
                        .expectedStream("events")
                        .build())
        productClient.send("events.four", "jkl".getBytes(StandardCharsets.UTF_8),
                PublishOptions.builder()
                        .stream("events")
                        .messageId("id00002")
                        .expectedStream("events")
                        .expectedLastMsgId("id00001")
                        .expectedLastSequence(pa.getSeqno())
                        .build())

        PullConsumerHelper pullConsumerHelper = applicationContext.getBean(PullConsumerHelper)

        then:
        waitFor {
            pullConsumerHelper.pullMessages().size() == 2
        }

        cleanup:
        JetStreamManagement jsm = applicationContext.getBean(JetStreamManagement)
        jsm.deleteStream("events")
    }
}
