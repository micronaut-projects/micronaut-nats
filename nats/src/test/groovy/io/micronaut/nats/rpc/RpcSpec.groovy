package io.micronaut.nats.rpc

import io.micronaut.context.ApplicationContext
import io.micronaut.nats.AbstractNatsTest

/**
 *
 * @author jgrimm
 */
class RpcSpec extends AbstractNatsTest {

    void "test rpc call"() {
        ApplicationContext context = startContext()
        Publisher producer = context.getBean(Publisher)

        expect:
        producer.rpcCall("hello").blockFirst() == "HELLO"
        producer.rpcCallMono("hello").block() == "HELLO"
        producer.rpcCallMono(null).block() == null

        cleanup:
        context.close()
    }
}
