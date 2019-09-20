package com.github.grimmjo.micronaut.nats.annotation

import com.github.grimmjo.micronaut.nats.AbstractNatsTest
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import spock.util.concurrent.PollingConditions

class PojoBindingSpec extends AbstractNatsTest {

    void "test simple producing and consuming with a pojo"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyProducer producer = applicationContext.getBean(MyProducer)
        MyConsumer consumer = applicationContext.getBean(MyConsumer)

        when:
        producer.go(new Person(name: "abc"))

        then:
        conditions.eventually {
            consumer.messages.size() == 1
            consumer.messages[0].name == "abc"
        }

        cleanup:
        applicationContext.close()
    }

    void "test simple producing and consuming with a list of pojos"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 3)
        MyListProducer producer = applicationContext.getBean(MyListProducer)
        MyListConsumer consumer = applicationContext.getBean(MyListConsumer)

        when:
        producer.go([new Person(name: "abc"), new Person(name: "def")])

        then:
        conditions.eventually {
            consumer.messages.size() == 2
            consumer.messages[0].name == "abc"
            consumer.messages[1].name == "def"
        }

        cleanup:
        applicationContext.close()
    }

    static class Person {
        String name
    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("pojo")
        void go(Person data)

    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsListener
    static class MyConsumer {

        public static List<Person> messages = []

        @Subject("pojo")
        void listen(Person data) {
            messages.add(data)
        }
    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsClient
    static interface MyListProducer {

        @Subject("pojo-list")
        void go(List<Person> data)

    }

    @Requires(property = "spec.name", value = "PojoBindingSpec")
    @NatsListener
    static class MyListConsumer {

        public static List<Person> messages = []

        @Subject("pojo-list")
        void listen(List<Person> data) {
            messages.addAll(data)
        }
    }
}
