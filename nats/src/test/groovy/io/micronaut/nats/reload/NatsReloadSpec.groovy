package io.micronaut.nats.reload

import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultBeanContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.reload.ClassChange
import io.micronaut.context.reload.ClassChangeEvent
import io.micronaut.context.reload.ReloadStrategy
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.nats.annotation.NatsClient
import io.micronaut.nats.annotation.NatsListener
import io.micronaut.nats.annotation.Subject
import io.micronaut.nats.bind.NatsBinderRegistry
import io.micronaut.nats.intercept.NatsConsumerAdvice
import io.micronaut.nats.intercept.NatsIntroductionAdvice
import io.micronaut.nats.jetstream.annotation.JetStreamClient
import io.micronaut.nats.jetstream.annotation.JetStreamListener
import io.micronaut.nats.jetstream.annotation.PushConsumer
import io.micronaut.nats.jetstream.intercept.JetStreamIntroductionAdvice
import io.micronaut.nats.jetstream.intercept.JetStreamPushConsumerAdvice
import io.micronaut.nats.serdes.NatsMessageSerDes
import io.micronaut.nats.serdes.NatsMessageSerDesRegistry
import io.nats.client.Dispatcher
import io.nats.client.JetStreamManagement
import io.nats.client.Message
import io.nats.client.api.AckPolicy
import io.nats.client.api.PublishAck
import jakarta.inject.Singleton
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CopyOnWriteArrayList

/**
 * The development reloader against a NATS server with JetStream.
 */
class NatsReloadSpec extends Specification {

    private static final String RELOADER = 'io.micronaut.nats.intercept.DevelopmentNatsReloader'
    static final String SUBJECT = 'devreload.core'
    static final String STREAM = 'devreload'
    static final String JS_SUBJECT = 'devreload.js.one'

    static GenericContainer natsContainer = new GenericContainer("nats:latest")
        .withCommand("--js")
        .withExposedPorts(4222)
        .waitingFor(Wait.forListeningPort())

    static {
        natsContainer.start()
    }

    ApplicationContext context
    PollingConditions conditions = new PollingConditions(timeout: 10)

    void cleanup() {
        if (context != null) {
            try {
                context.getBean(JetStreamManagement).deleteStream(STREAM)
            } catch (Exception ignored) {
                // not created
            }
            context.close()
        }
    }

    void "in development mode an in-place change of a listener class restarts the consumers on new advices and a new listener, and a restart or an unrelated change does not"() {
        given:
        devContext(true)
        ReloadListener listener = context.getBean(ReloadListener)
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        JetStreamPushConsumerAdvice pushAdvice = context.getBean(JetStreamPushConsumerAdvice)
        Dispatcher first = dispatcher()

        expect: 'the reloader exists only in development mode'
        context.containsBean(reloader())

        when:
        sendBoth('one')

        then:
        conditions.eventually { assert listener.received == ['one'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one'] }

        when: 'the application restarts: the new context starts its own consumers'
        context.publishEvent(classChange([ReloadListener.classLoader] as Set, [], ReloadStrategy.RESTART))

        then:
        context.getBean(NatsConsumerAdvice).is(advice)
        context.getBean(JetStreamPushConsumerAdvice).is(pushAdvice)
        first.active

        when: 'a class that is not a listener is redefined in place'
        context.publishEvent(classChange([] as Set, [new ClassChange(NatsReloadSpec.name, ClassChange.Kind.MODIFIED)], ReloadStrategy.RELOAD))

        then:
        context.getBean(NatsConsumerAdvice).is(advice)
        context.getBean(ReloadListener).is(listener)
        first.active

        when: 'the listener class is redefined in place'
        context.publishEvent(classChange([] as Set, [new ClassChange(ReloadListener.name, ClassChange.Kind.MODIFIED)], ReloadStrategy.RELOAD))
        ReloadListener recreated = context.getBean(ReloadListener)

        then: 'the dispatcher of the replaced code is closed, and the advices and the listener are new'
        !first.active
        !context.getBean(NatsConsumerAdvice).is(advice)
        !context.getBean(JetStreamPushConsumerAdvice).is(pushAdvice)
        !recreated.is(listener)
        dispatcher().active

        when:
        sendBoth('two')

        then: 'messages reach the new listener only, and the push consumer bound its durable again'
        conditions.eventually { assert recreated.received == ['two'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one', 'two'] }
        listener.received == ['one']
    }

    void "in development mode a serializer change or a retired classloader recreates the registries, the client advices and the clients, and restarts the consumers"() {
        given:
        devContext(true)
        ReloadClient client = context.getBean(ReloadClient)
        NatsMessageSerDesRegistry serDes = context.getBean(NatsMessageSerDesRegistry)
        NatsBinderRegistry binders = context.getBean(NatsBinderRegistry)
        NatsIntroductionAdvice publishers = context.getBean(NatsIntroductionAdvice)
        JetStreamIntroductionAdvice jsPublishers = context.getBean(JetStreamIntroductionAdvice)
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        ReloadListener listener = context.getBean(ReloadListener)
        Dispatcher first = dispatcher()

        when: 'a serializer is redefined in place'
        context.publishEvent(classChange([] as Set, [new ClassChange(ReloadSerDes.name, ClassChange.Kind.MODIFIED)], ReloadStrategy.RELOAD))

        then: 'the registries and the client advices are new, and so are the beans that received them'
        !context.getBean(NatsMessageSerDesRegistry).is(serDes)
        !context.getBean(NatsBinderRegistry).is(binders)
        !context.getBean(NatsIntroductionAdvice).is(publishers)
        !context.getBean(JetStreamIntroductionAdvice).is(jsPublishers)
        !context.getBean(ReloadClient).is(client)
        !context.getBean(NatsConsumerAdvice).is(advice)

        and: 'the consumers are started again, the listener bean being unchanged'
        !first.active
        context.getBean(ReloadListener).is(listener)

        when:
        sendBoth('one')

        then: 'each message is received once'
        conditions.eventually { assert listener.received == ['one'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one'] }

        when: 'a reload retires a classloader'
        serDes = context.getBean(NatsMessageSerDesRegistry)
        advice = context.getBean(NatsConsumerAdvice)
        context.publishEvent(classChange([ReloadListener.classLoader] as Set, [], ReloadStrategy.RELOAD))

        then:
        !context.getBean(NatsMessageSerDesRegistry).is(serDes)
        !context.getBean(NatsConsumerAdvice).is(advice)

        when:
        sendBoth('two')

        then:
        conditions.eventually { assert listener.received == ['one', 'two'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one', 'two'] }
    }

    void "in development mode a listener definition registered while running restarts the consumers once"() {
        given:
        devContext(true)
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        Dispatcher first = dispatcher()
        BeanDefinition<?> definition = context.getBeanDefinition(ReloadPushListener)

        when: 'the launcher swaps the definition of the push listener for another of the same class'
        ((DefaultBeanContext) context).notifyDefinitionChange([definition], [definition])

        then: 'every consumer started before is closed, and one consumes for each listener'
        !first.active
        !context.getBean(NatsConsumerAdvice).is(advice)

        when:
        sendBoth('one')

        then:
        conditions.eventually { assert context.getBean(ReloadListener).received == ['one'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one'] }
    }

    void "in development mode a client definition registered while running recreates the client advices"() {
        given:
        devContext(true)
        NatsIntroductionAdvice publishers = context.getBean(NatsIntroductionAdvice)
        ReloadClient client = context.getBean(ReloadClient)
        BeanDefinition<?> definition = context.getBeanDefinition(ReloadClient)

        when:
        ((DefaultBeanContext) context).notifyDefinitionChange([definition], [definition])

        then:
        !context.getBean(NatsIntroductionAdvice).is(publishers)
        !context.getBean(ReloadClient).is(client)

        when:
        sendBoth('one')

        then:
        conditions.eventually { assert context.getBean(ReloadListener).received == ['one'] }
    }

    void "in development mode the context starts the consumers again on new advices when another module recreates a bean they received, without the reloader"() {
        given:
        devContext(true)
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        JetStreamPushConsumerAdvice pushAdvice = context.getBean(JetStreamPushConsumerAdvice)
        ReloadListener listener = context.getBean(ReloadListener)
        Dispatcher first = dispatcher()

        when: 'a module recreates the binder registry for a change of its own, which destroys both advices'
        context.recreate(context.getBean(NatsBinderRegistry))

        then: 'the context created them again at once and gave them their methods: no class change follows'
        !first.active
        !context.getBean(NatsConsumerAdvice).is(advice)
        !context.getBean(JetStreamPushConsumerAdvice).is(pushAdvice)

        when:
        sendBoth('one')

        then:
        conditions.eventually { assert listener.received == ['one'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one'] }
    }

    void "in development mode a context that does not track bean dependencies keeps the consumers running"() {
        given:
        devContext(false)
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        Dispatcher first = dispatcher()

        expect:
        context.containsBean(reloader())

        when:
        context.publishEvent(classChange([] as Set, [new ClassChange(ReloadListener.name, ClassChange.Kind.MODIFIED)], ReloadStrategy.RELOAD))

        then:
        context.getBean(NatsConsumerAdvice).is(advice)
        first.active
    }

    void "outside development mode there is no reloader, and the same beans keep their consumers through class changes"() {
        given:
        context = ApplicationContext.run(properties(), "test")
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        NatsMessageSerDesRegistry serDes = context.getBean(NatsMessageSerDesRegistry)
        ReloadListener listener = context.getBean(ReloadListener)
        Dispatcher first = dispatcher()

        expect:
        !context.containsBean(reloader())

        when:
        context.publishEvent(classChange([ReloadListener.classLoader] as Set, [new ClassChange(ReloadListener.name, ClassChange.Kind.MODIFIED)], ReloadStrategy.RELOAD))

        then:
        context.getBean(NatsConsumerAdvice).is(advice)
        context.getBean(NatsMessageSerDesRegistry).is(serDes)
        context.getBean(ReloadListener).is(listener)
        first.active

        when:
        sendBoth('one')

        then: 'the listeners consume as before'
        conditions.eventually { assert listener.received == ['one'] }
        conditions.eventually { assert context.getBean(ReloadPushListener).received == ['one'] }
    }

    private void devContext(boolean track) {
        context = ApplicationContext.builder()
            .properties(properties() + ['micronaut.dev.enabled': true])
            .environments("test")
            .trackBeanDependencies(track)
            .start()
    }

    private Map<String, Object> properties() {
        return ["nats.default.addresses"                                   : ["nats://localhost:${natsContainer.getMappedPort(4222)}".toString()],
                "spec.name"                                                : 'NatsReloadSpec',
                ("nats.default.jetstream.streams." + STREAM + ".storage-type"): "Memory",
                ("nats.default.jetstream.streams." + STREAM + ".subjects")    : ['devreload.js.>']]
    }

    private void sendBoth(String value) {
        context.getBean(ReloadClient).send(value)
        context.getBean(ReloadJetStreamClient).send(value.bytes)
    }

    /**
     * The dispatcher of the core NATS listener.
     */
    private Dispatcher dispatcher() {
        NatsConsumerAdvice advice = context.getBean(NatsConsumerAdvice)
        Set<String> ids = advice.consumerIds
        assert ids.size() == 1
        return (Dispatcher) advice.getConsumer(ids.first())
    }

    private static Class<?> reloader() {
        return Class.forName(RELOADER)
    }

    private ClassChangeEvent classChange(Set<ClassLoader> retired, List<ClassChange> changes, ReloadStrategy strategy) {
        return new ClassChangeEvent(this, 1, retired, NatsReloadSpec.classLoader, changes, strategy)
    }

    @NatsListener
    @Requires(property = 'spec.name', value = 'NatsReloadSpec')
    static class ReloadListener {
        final List<String> received = new CopyOnWriteArrayList<>()

        @Subject(SUBJECT)
        void receive(String value) {
            received.add(value)
        }
    }

    @JetStreamListener
    @Requires(property = 'spec.name', value = 'NatsReloadSpec')
    static class ReloadPushListener {
        final List<String> received = new CopyOnWriteArrayList<>()

        @PushConsumer(value = STREAM, subject = JS_SUBJECT, durable = 'devreload', ackPolicy = AckPolicy.All)
        void receive(byte[] value) {
            received.add(new String(value))
        }
    }

    @NatsClient
    @Requires(property = 'spec.name', value = 'NatsReloadSpec')
    static interface ReloadClient {
        @Subject(SUBJECT)
        void send(String value)
    }

    @JetStreamClient
    @Requires(property = 'spec.name', value = 'NatsReloadSpec')
    static interface ReloadJetStreamClient {
        @Subject(JS_SUBJECT)
        PublishAck send(@MessageBody byte[] value)
    }

    @Singleton
    @Requires(property = 'spec.name', value = 'NatsReloadSpec')
    static class ReloadSerDes implements NatsMessageSerDes<UUID> {
        @Override
        UUID deserialize(Message message, Argument<UUID> type) {
            return UUID.fromString(new String(message.data))
        }

        @Override
        byte[] serialize(UUID data) {
            return data?.toString()?.bytes
        }

        @Override
        boolean supports(Argument<UUID> type) {
            return type.type == UUID
        }
    }
}
