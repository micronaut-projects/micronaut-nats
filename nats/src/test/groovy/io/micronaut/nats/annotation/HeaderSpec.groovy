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
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.nats.AbstractNatsTest
import io.nats.client.impl.Headers
import spock.util.concurrent.PollingConditions

class HeaderSpec extends AbstractNatsTest {

    void "test simple headers"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 10)
        MyProducer producer = applicationContext.getBean(MyProducer)
        SingleTokenConsumer singleTokenConsumer = applicationContext.getBean(SingleTokenConsumer)

        when:
        producer.go(true, "myHeader1")
        producer.go(true, "myHeader2")

        Headers headers = new Headers();
        headers.put("test", "1","2","3","4")
        headers.put("test1", "5");
        producer.testHeaders(false, headers)

        producer.testHeadersList(false, ["test1", "test2"])

        then:
        conditions.eventually {
            singleTokenConsumer.simpleHeaders.size() == 2
            singleTokenConsumer.simpleHeaders[0] == 'myHeader1'
            singleTokenConsumer.simpleHeaders[1] == 'myHeader2'

            singleTokenConsumer.natsHeaders.size() == 5
            singleTokenConsumer.natsHeaders == ["1", "2", "3", "4", "5"]

            singleTokenConsumer.headerList.size() == 2
            singleTokenConsumer.headerList == ["test1", "test2"]
            singleTokenConsumer.headerListString == "test1,test2"

        }

        cleanup:
        applicationContext.close()
    }

    @Requires(property = "spec.name", value = "HeaderSpec")
    @NatsClient
    static interface MyProducer {

        @Subject("header")
        void go(@MessageBody Boolean data, @MessageHeader myHeader)

        @Subject("headers")
        void testHeaders(@MessageBody Boolean data, Headers headers)

        @Subject("headersList")
        void testHeadersList(@MessageBody Boolean data, @MessageHeader List<String> headersList)
    }

    @Requires(property = "spec.name", value = "HeaderSpec")
    @NatsListener
    static class SingleTokenConsumer {

        public static List<String> simpleHeaders = []
        public static List<String> natsHeaders = []
        public static List<String> headerList = []
        public static String headerListString


        @Subject("header")
        void listen(@Nullable Boolean data, @MessageHeader String myHeader) {
            simpleHeaders.add(myHeader)
        }

        @Subject("headers")
        void listenHeader(@MessageBody Boolean data, Headers headers) {
            headers.forEach((k,v) -> natsHeaders.addAll(v));
        }

        @Subject("headersList")
        void listenHeaderList(@MessageBody Boolean data, @MessageHeader List<String> headersList) {
            headerList.addAll(headersList)
        }

        @Subject("headersList")
        void listenHeaderList(@MessageBody Boolean data, @MessageHeader String headersList) {
            headerListString = headersList
        }
    }

}
