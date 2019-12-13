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
        producer.rpcBlocking("hello") == "HELLO"
        producer.rpcCallMaybe("hello").blockingGet() == "HELLO"
        producer.rpcCallMaybe(null).blockingGet() == null
        producer.rpcCallSingle("hello").blockingGet() == "HELLO"
        producer.rpcBlocking("world") == "WORLD"

        when:
        producer.rpcCallSingle(null).blockingGet() == null

        then:
        thrown(NoSuchElementException)

        cleanup:
        context.close()
    }
}
