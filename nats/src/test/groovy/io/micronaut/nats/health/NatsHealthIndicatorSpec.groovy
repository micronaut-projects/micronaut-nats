package io.micronaut.nats.health


import io.micronaut.context.ApplicationContext
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.nats.AbstractNatsTest
import io.reactivex.Single

/**
 *
 * @author jgrimm
 */
class NatsHealthIndicatorSpec extends AbstractNatsTest {

    void "test nats health indicator"() {
        given:
        ApplicationContext context = startContext()

        when:
        NatsHealthIndicator healthIndicator = context.getBean(NatsHealthIndicator)
        HealthResult result = Single.fromPublisher(healthIndicator.result).blockingGet()

        then:
        result.status == HealthStatus.UP
        result.details.servers[0] == "nats://localhost:${natsContainer.getMappedPort(4222)}".toString()

        cleanup:
        context.close();
    }

    void "test nats health indicator status down"() {
        given:
        ApplicationContext context = startContext()

        when:
        NatsHealthIndicator healthIndicator = context.getBean(NatsHealthIndicator)
        natsContainer.stop()
        HealthResult result = Single.fromPublisher(healthIndicator.result).blockingGet()

        then:
        result.status == HealthStatus.DOWN

        cleanup:
        natsContainer.start()
        context.close();
    }
}
