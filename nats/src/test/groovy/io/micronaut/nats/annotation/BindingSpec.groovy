/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.annotation


import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Nullable
import io.micronaut.nats.AbstractNatsTest
import spock.util.concurrent.PollingConditions

class BindingSpec extends AbstractNatsTest {

    void "test simple producing and consuming with a boolean"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 10)
        MyProducer producer = applicationContext.getBean(MyProducer)
        SingleTokenConsumer singleTokenConsumer = applicationContext.getBean(SingleTokenConsumer)
        MultipleTokensConsumer multipleTokensConsumer = applicationContext.getBean(MultipleTokensConsumer)
        AllConsumer allConsumer = applicationContext.getBean(AllConsumer)
        CombinedConsumer combinedConsumer = applicationContext.getBean(CombinedConsumer)

        when:
        producer.go("time.us",true)
        producer.go("time.us.east",true)
        producer.go("time.us.east.atlanta",true)

        then:
        conditions.eventually {
            singleTokenConsumer.messages.size() == 1
            multipleTokensConsumer.messages.size() == 2
            allConsumer.messages.size() == 3
            combinedConsumer.messages.size() == 1
        }

        cleanup:
        applicationContext.close()
    }

    @Requires(property = "spec.name", value = "BindingSpec")
    @NatsClient
    static interface MyProducer {

        void go(@Subject String subject, Boolean data)

    }

    @Requires(property = "spec.name", value = "BindingSpec")
    @NatsListener
    static class SingleTokenConsumer {

        public static List<Boolean> messages = []

        @Subject("time.*.east")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }

    }

    @Requires(property = "spec.name", value = "BindingSpec")
    @NatsListener
    static class MultipleTokensConsumer {

        public static List<Boolean> messages = []

        @Subject("time.us.>")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }

    }

    @Requires(property = "spec.name", value = "BindingSpec")
    @NatsListener
    static class AllConsumer {

        public static List<Boolean> messages = []

        @Subject(">")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }

    }

    @Requires(property = "spec.name", value = "BindingSpec")
    @NatsListener
    static class CombinedConsumer {

        public static List<Boolean> messages = []

        @Subject("*.*.east.>")
        void listen(@Nullable Boolean data) {
            messages.add(data)
        }

    }
}
