/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.nats.jetstream.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.messaging.annotation.MessageMapping;
import io.micronaut.nats.annotation.NatsConnection;
import io.micronaut.nats.annotation.Subject;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

/**
 * Used to specify which stream should be used for messages.
 *
 * @author Joachim Grimm
 * @since 4.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
@Bindable
@Executable
@MessageMapping
@Inherited
public @interface PushConsumer {

    /**
     * @return The subject to subscribe to.
     */
    @AliasFor(annotation = MessageMapping.class, member = "value")
    String value() default "";

    /**
     * @return The connection to use
     * @see NatsConnection#connection()
     */
    @AliasFor(annotation = NatsConnection.class, member = "connection")
    String connection() default "";

    @AliasFor(annotation = Subject.class, member = "value")
    String subject();

    /**
     * @return the durable name
     * @see io.nats.client.api.ConsumerConfiguration.Builder#durable(String)
     */
    String durable();

    /**
     * @return the deliver policy
     * @see io.nats.client.api.ConsumerConfiguration.Builder#deliverPolicy(DeliverPolicy)
     */
    DeliverPolicy deliverPolicy() default DeliverPolicy.All;

    /**
     * @return the start sequence
     * @see io.nats.client.api.ConsumerConfiguration.Builder#startSequence(Long)
     */
    long startSequence();

    /**
     * @return the deliver subject
     * @see io.nats.client.api.ConsumerConfiguration.Builder#deliverSubject(String)
     */
    String deliverSubject();

    /**
     * @return the acknowledgment policy
     * @see io.nats.client.api.ConsumerConfiguration.Builder#ackPolicy(AckPolicy)
     */
    AckPolicy ackPolicy();

    /**
     * @return the acknowledgment wait time in millis
     * @see io.nats.client.api.ConsumerConfiguration.Builder#ackWait(long)
     */
    long ackWait();

    /**
     * @return the replay policy
     * @see io.nats.client.api.ConsumerConfiguration.Builder#replayPolicy(ReplayPolicy)
     */
    ReplayPolicy replayPolicy();

    /**
     * @return the max deliver
     * @see io.nats.client.api.ConsumerConfiguration.Builder#maxDeliver(long)
     */
    long maxDeliver();

    /**
     * @return the filter subject
     * @see io.nats.client.api.ConsumerConfiguration.Builder#filterSubject(String)
     */
    String filterSubject();

    /**
     * @return the rate limit
     * @see io.nats.client.api.ConsumerConfiguration.Builder#rateLimit(long)
     */
    long rateLimit();

    /**
     * @return the sample frequency
     * @see io.nats.client.api.ConsumerConfiguration.Builder#sampleFrequency(String)
     */
    String sampleFrequency();

    /**
     * @return the idle heartbeat in millis
     * @see io.nats.client.api.ConsumerConfiguration.Builder#idleHeartbeat(long)
     */
    long idleHeartbeat();

    /**
     * @return the flow control in millis
     * @see io.nats.client.api.ConsumerConfiguration.Builder#flowControl(long)
     */
    long flowControl();

    /**
     * @return the back off in millis
     * @see io.nats.client.api.ConsumerConfiguration.Builder#backoff(long...)
     */
    long[] backoff();

    /**
     * @return the header only
     * @see io.nats.client.api.ConsumerConfiguration.Builder#headersOnly(Boolean)
     */
    boolean headersOnly();

    /**
     * @return the max acknowledgment pending
     * @see io.nats.client.api.ConsumerConfiguration.Builder#maxAckPending(long)
     */
    long maxAckPending();

    /**
     * @return the deliver group
     * @see io.nats.client.api.ConsumerConfiguration.Builder#deliverGroup(String)
     */
    String deliverGroup();

    /**
     * @return the description
     * @see io.nats.client.api.ConsumerConfiguration.Builder#description(String)
     */
    String description();

    /**
     * @return the queue of the consumer
     */
    String queue() default "";
}
