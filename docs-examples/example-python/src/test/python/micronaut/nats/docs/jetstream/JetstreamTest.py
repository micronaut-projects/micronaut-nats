from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
try:
    from io.nats.client import JetStreamManagement, PublishOptions
except ImportError:  # TODO(python): packages under `io.` other than `io.micronaut` cannot be imported at runtime
    from nats.client import JetStreamManagement, PublishOptions

from .ProductClient import ProductClient
from .ProductListener import ProductListener
from .PullConsumerHelper import PullConsumerHelper


@Property(name="spec.name", value="JetstreamTest")
@MicronautTest(environments=["nats"])
class JetstreamTest:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]
    pull_consumer_helper: Annotated[PullConsumerHelper, Inject]
    jsm: Annotated[JetStreamManagement, Inject]

    @Test
    def simple_publisher(self):
        # tag::producer[]
        pa = self.product_client.send("myevents.one", "ghi".encode("utf-8"),
                                      PublishOptions.builder()
                                      .messageId("id00001")
                                      .expectedStream("myevents")
                                      .build())
        self.product_client.send("myevents.two", "jkl".encode("utf-8"),
                                 PublishOptions.builder()
                                 .messageId("id00002")
                                 .expectedStream("myevents")
                                 .expectedLastMsgId("id00001")
                                 .expectedLastSequence(pa.getSeqno())
                                 .build())
        # end::producer[]

        for _ in range(600):
            if len(self.product_listener.message_lengths) == 2 and self.jsm.getStreamInfo("myevents").getStreamState().getMsgCount() == 2:
                break
            sleep(0.1)
        assert len(self.product_listener.message_lengths) == 2
        assert self.jsm.getStreamInfo("myevents").getStreamState().getMsgCount() == 2

    @Test
    def pull_consumer(self):
        pa = self.product_client.send("events.three", "ghi".encode("utf-8"),
                                      PublishOptions.builder()
                                      .messageId("id00001")
                                      .expectedStream("events")
                                      .build())
        self.product_client.send("events.four", "jkl".encode("utf-8"),
                                 PublishOptions.builder()
                                 .messageId("id00002")
                                 .expectedStream("events")
                                 .expectedLastMsgId("id00001")
                                 .expectedLastSequence(pa.getSeqno())
                                 .build())

        pulled = 0
        for _ in range(60):
            pulled = len(self.pull_consumer_helper.pull_messages())
            if pulled == 2:
                break
            sleep(0.1)
        assert pulled == 2
