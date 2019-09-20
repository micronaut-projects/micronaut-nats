package com.github.grimmjo.micronaut.nats.annotation

import com.github.grimmjo.micronaut.nats.AbstractNatsTest
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.Body
import io.reactivex.Completable
import spock.util.concurrent.PollingConditions
/**
 *
 * @author jgrimm
 */
class BasicAopSpec extends AbstractNatsTest {

    void "test simple producing and consuming"() {
        ApplicationContext context = startContext()
        MyProducer producer = context.getBean(MyProducer)
        MyConsumer consumer = context.getBean(MyConsumer)
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
        producer.go("abc".bytes)
        producer.go("def".bytes)
//        boolean success = producer.goConfirm("def".bytes).blockingAwait(2, TimeUnit.SECONDS)

        then:
        conditions.eventually {
            consumer.messages.size() == 2
            consumer.messages[0] == "abc".bytes
            consumer.messages[1] == "def".bytes
        }

        cleanup:
        context.close()
    }

    @Requires(property = "spec.name", value = "BasicAopSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("abc")
        void go(@Body byte[] data)

        @Subject("abc")
        Completable goConfirm(byte[] data)
    }

    @Requires(property = "spec.name", value = "BasicAopSpec")
    @NatsListener
    static class MyConsumer {

        public static List<byte[]> messages = []

        @Subject("abc")
        void listen(byte[] data) {
            messages.add(data)
        }
    }
}
