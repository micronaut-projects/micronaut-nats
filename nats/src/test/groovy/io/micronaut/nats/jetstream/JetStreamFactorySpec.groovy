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
package io.micronaut.nats.jetstream

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.nats.annotation.NatsConnection
import io.nats.client.JetStream
import io.nats.client.JetStreamManagement

class JetStreamFactorySpec extends AbstractJetstreamTest {

    void "simple factory test"() {
        given:
        ApplicationContext applicationContext = startContext()
        JetStreamManagement jsm = applicationContext.getBean(JetStreamManagement, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION))

        expect:
        jsm != null
        applicationContext.getBean(JetStream, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION)) != null
        jsm.getStreamInfo("widgets") != null

        cleanup:
        applicationContext.close()
    }


}
