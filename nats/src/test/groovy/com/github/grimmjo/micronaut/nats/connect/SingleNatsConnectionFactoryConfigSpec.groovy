/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.grimmjo.micronaut.nats.connect


import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.nats.client.Connection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Shared
import spock.lang.Specification
/**
 *
 * @author jgrimm
 */
class SingleNatsConnectionFactoryConfigSpec extends Specification {

    @Shared
    GenericContainer natsContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    void "default nats configuration"() {
        given:
        natsContainer.start()
        String address = "nats://localhost:${natsContainer.getMappedPort(4222)}"
//        ApplicationContext context = startContext()
        ApplicationContext context = ApplicationContext.run(
                ["spec.name"   : getClass().simpleName,
                 "nats.address": address])

        expect:
        context.getBean(SingleNatsConnectionFactoryConfig, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).address.get() == address
        context.getBean(Connection, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).getServers().contains(address)
        context.getBean(Connection, Qualifiers.byName(SingleNatsConnectionFactoryConfig.DEFAULT_NAME)).getConnectedUrl() == address

        cleanup:
        context.stop()
        natsContainer.stop()

    }
}
