/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.nats.dev;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs an application with a NATS listener and a JetStream push consumer through the development runtime, against a
 * NATS server, and edits the listeners. A change applied in place restarts the consumers on the new listeners; the
 * restart closes the dispatchers of the retired generation as its context stops, and the next generation subscribes on
 * the same connection, which development mode retains until a change under {@code nats} releases it. Nothing of the
 * retired generation stays reachable.
 */
class NatsReloadTest {

    private static final int CLIENT_PORT = 4222;
    private static final int MONITOR_PORT = 8222;
    private static final String SUBJECT = "devreload.core";
    private static final String STREAM = "devreload";
    private static final String JS_SUBJECT = "devreload.js.one";

    private static final String LISTENER = """
        package example;

        import io.micronaut.nats.annotation.NatsListener;
        import io.micronaut.nats.annotation.Subject;

        import java.util.List;
        import java.util.concurrent.CopyOnWriteArrayList;

        @NatsListener
        public class Listener {
            private final List<String> received = new CopyOnWriteArrayList<>();

            @Subject("%s")
            public void receive(String value) {
                received.add("%s " + value);
            }

            public List<String> received() {
                return received;
            }
        }
        """;

    private static final String PUSH_LISTENER = """
        package example;

        import io.micronaut.nats.jetstream.annotation.JetStreamListener;
        import io.micronaut.nats.jetstream.annotation.PushConsumer;
        import io.nats.client.api.AckPolicy;

        import java.nio.charset.StandardCharsets;
        import java.util.List;
        import java.util.concurrent.CopyOnWriteArrayList;

        @JetStreamListener
        public class PushListener {
            private final List<String> received = new CopyOnWriteArrayList<>();

            @PushConsumer(value = "%s", subject = "%s", durable = "devreload", ackPolicy = AckPolicy.All)
            public void receive(byte[] value) {
                received.add("%s " + new String(value, StandardCharsets.UTF_8));
            }

            public List<String> received() {
                return received;
            }
        }
        """;

    private static GenericContainer<?> nats;

    @TempDir
    Path project;

    @BeforeAll
    static void startServer() {
        nats = new GenericContainer<>(DockerImageName.parse("nats:latest"))
            .withCommand("--js", "-m", String.valueOf(MONITOR_PORT))
            .withExposedPorts(CLIENT_PORT, MONITOR_PORT)
            .waitingFor(Wait.forListeningPorts(CLIENT_PORT, MONITOR_PORT));
        nats.start();
    }

    @AfterAll
    static void stopServer() {
        nats.stop();
    }

    @Test
    void anInPlaceChangeRestartsTheConsumersAndARestartClosesThemAndLeavesTheRetiredGenerationCollectable() throws Exception {
        String address = "nats://" + nats.getHost() + ":" + nats.getMappedPort(CLIENT_PORT);
        try (Connection connection = Nats.connect(address);
             ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            JetStreamManagement management = connection.jetStreamManagement();
            JetStream jetStream = connection.jetStream();
            int baseline = connections();

            harness.property("nats.default.addresses", address);
            // the application creates the stream, as JetStream beans exist only for a connection with JetStream configuration
            harness.property("nats.default.jetstream.streams." + STREAM + ".storage-type", "Memory");
            harness.property("nats.default.jetstream.streams." + STREAM + ".subjects", "devreload.js.>");
            harness.source("example.Listener", LISTENER.formatted(SUBJECT, "first"));
            harness.source("example.PushListener", PUSH_LISTENER.formatted(STREAM, JS_SUBJECT, "first"));
            harness.start();
            assertReloaderPresent(harness.context());
            awaitTrue("the first generation connected", () -> connections() == baseline + 1);

            publish(connection, jetStream, "one");
            awaitTrue("the first generation receives", () -> received(harness.context(), "example.Listener").contains("first one")
                && received(harness.context(), "example.PushListener").contains("first one"));
            ReloadTck.assertFollowsReload(harness, context -> bean(context, "example.Listener"));
            int subscriptions = subscriptions();

            // the listener classes changed in place: the consumers are restarted on new listener beans
            Object listener = bean(harness.context(), "example.Listener");
            Object pushListener = bean(harness.context(), "example.PushListener");
            long inPlaceStart = System.nanoTime();
            changedInPlace(harness, "example.Listener", "example.PushListener");
            assertNotSame(listener, bean(harness.context(), "example.Listener"));
            assertNotSame(pushListener, bean(harness.context(), "example.PushListener"));
            List<String> retiredCore = received(listener);
            List<String> retiredPush = received(pushListener);
            listener = null;
            pushListener = null;
            publish(connection, jetStream, "in-place");
            awaitTrue("the new listeners receive", () -> received(harness.context(), "example.Listener").equals(List.of("first in-place"))
                && received(harness.context(), "example.PushListener").equals(List.of("first in-place")));
            System.out.println("The restarted consumers received a message " + millisSince(inPlaceStart) + " ms after the change");
            assertEquals(List.of("first one"), retiredCore, "the closed dispatcher received nothing more");
            assertEquals(List.of("first one"), retiredPush, "the closed push consumer received nothing more");
            assertEquals(subscriptions, subscriptions(), "the server has as many subscriptions as before the change");

            harness.source("example.Listener", LISTENER.formatted(SUBJECT, "second"));
            harness.source("example.PushListener", PUSH_LISTENER.formatted(STREAM, JS_SUBJECT, "second"));
            List<String> firstCore = received(harness.context(), "example.Listener");
            List<String> firstPush = received(harness.context(), "example.PushListener");
            Connection retained = harness.context().getBean(Connection.class);
            JetStream retainedJetStream = harness.context().getBean(JetStream.class);
            long reloadStart = System.nanoTime();
            harness.reload();
            assertEquals(2, harness.generation());
            assertReloaderPresent(harness.context());

            // the second generation subscribes on the connection of the first: the server never saw another one
            ReloadTck.assertRetained(harness, retained);
            ReloadTck.assertRetained(harness, retainedJetStream);
            assertSame(retained, harness.context().getBean(Connection.class));
            assertSame(retainedJetStream, harness.context().getBean(JetStream.class));
            assertEquals(Connection.Status.CONNECTED, retained.getStatus());
            assertEquals(baseline + 1, connections(), "the server has the one retained connection");
            publish(connection, jetStream, "two");
            awaitTrue("the second generation receives", () -> received(harness.context(), "example.Listener").contains("second two")
                && received(harness.context(), "example.PushListener").contains("second two"));
            long restarted = millisSince(reloadStart);
            assertTrue(restarted < 30_000, "the second generation received a message " + restarted + " ms after the reload started");
            System.out.println("The second generation received a message " + restarted + " ms after the reload started");
            assertEquals(List.of("first in-place"), firstCore, "the retired listener received nothing more");
            assertEquals(List.of("first in-place"), firstPush, "the retired push consumer received nothing more");
            assertEquals(subscriptions, subscriptions(), "the server has as many subscriptions as before the restart");
            ReloadTck.assertFollowsReload(harness, context -> bean(context, "example.Listener"));

            // neither the dispatchers of the first generation, the retained connection, nor the development-only reloader keep it reachable
            ReloadTck.assertRetiredGenerationsCollected(harness);
            management.deleteStream(STREAM);
        }
    }

    @Test
    void aChangeUnderTheNatsPrefixReleasesTheRetainedConnection() throws Exception {
        String address = "nats://" + nats.getHost() + ":" + nats.getMappedPort(CLIENT_PORT);
        try (Connection connection = Nats.connect(address);
             ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            JetStreamManagement management = connection.jetStreamManagement();
            JetStream jetStream = connection.jetStream();
            int baseline = connections();

            harness.property("nats.default.addresses", address);
            harness.property("nats.default.jetstream.streams." + STREAM + ".storage-type", "Memory");
            harness.property("nats.default.jetstream.streams." + STREAM + ".subjects", "devreload.js.>");
            harness.source("example.Listener", LISTENER.formatted(SUBJECT, "first"));
            harness.source("example.PushListener", PUSH_LISTENER.formatted(STREAM, JS_SUBJECT, "first"));
            harness.start();
            awaitTrue("the first generation connected", () -> connections() == baseline + 1);
            Connection first = harness.context().getBean(Connection.class);
            JetStream firstJetStream = harness.context().getBean(JetStream.class);

            // the connection configuration changes, together with a class, so the application restarts
            harness.resource("application.properties", properties(address, "PT17S"));
            harness.source("example.Listener", LISTENER.formatted(SUBJECT, "second"));
            harness.source("example.PushListener", PUSH_LISTENER.formatted(STREAM, JS_SUBJECT, "second"));
            harness.reload();
            assertEquals(2, harness.generation());

            Connection second = harness.context().getBean(Connection.class);
            assertNotSame(first, second);
            assertNotSame(firstJetStream, harness.context().getBean(JetStream.class));
            assertEquals(Connection.Status.CLOSED, first.getStatus(), "the released connection is closed");
            assertEquals(Duration.ofSeconds(17), second.getOptions().getPingInterval());
            first = null;
            firstJetStream = null;
            awaitTrue("the server has the one connection of the second generation", () -> connections() == baseline + 1);

            publish(connection, jetStream, "two");
            awaitTrue("the second generation receives on its connection", () -> received(harness.context(), "example.Listener").equals(List.of("second two"))
                && received(harness.context(), "example.PushListener").equals(List.of("second two")));
            ReloadTck.assertRetiredGenerationsCollected(harness);
            management.deleteStream(STREAM);
        }
    }

    private static String properties(String address, String pingInterval) {
        // as the harness wrote them at the start, with the ping interval
        return "nats.default.addresses=" + address + "\n"
            + "nats.default.jetstream.streams." + STREAM + ".storage-type=Memory\n"
            + "nats.default.jetstream.streams." + STREAM + ".subjects=devreload.js.>\n"
            + "nats.default.ping-interval=" + pingInterval + "\n";
    }

    private static void publish(Connection connection, JetStream jetStream, String value) throws Exception {
        connection.publish(SUBJECT, value.getBytes(StandardCharsets.UTF_8));
        jetStream.publish(JS_SUBJECT, value.getBytes(StandardCharsets.UTF_8));
    }

    private static int connections() {
        return monitor("connz", "num_connections");
    }

    private static int subscriptions() {
        return monitor("subsz", "num_subscriptions");
    }

    private static int monitor(String endpoint, String field) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://" + nats.getHost() + ":" + nats.getMappedPort(MONITOR_PORT) + "/" + endpoint)).build(),
                HttpResponse.BodyHandlers.ofString());
            Matcher matcher = Pattern.compile('"' + field + "\"\\s*:\\s*(\\d+)").matcher(response.body());
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static long millisSince(long start) {
        return Duration.ofNanos(System.nanoTime() - start).toMillis();
    }

    /**
     * Tells the running generation that classes were redefined in place, as the development runtime does after it
     * redefined them. The context is not kept: a reference to it would keep the generation reachable.
     */
    private static void changedInPlace(ReloadHarness harness, String... classNames) {
        ApplicationContext context = harness.context();
        List<ClassChange> changes = java.util.Arrays.stream(classNames).map(name -> new ClassChange(name, ClassChange.Kind.MODIFIED)).toList();
        context.publishEvent(new ClassChangeEvent(NatsReloadTest.class, Set.of(), context.getClassLoader(), changes, ReloadStrategy.RELOAD));
    }

    private static void awaitTrue(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting until " + what);
            }
            Thread.sleep(100);
        }
    }

    private static void assertReloaderPresent(ApplicationContext context) {
        // the bean that follows changes in place exists in development mode only
        assertTrue(context.containsBean(type(context, "io.micronaut.nats.intercept.DevelopmentNatsReloader")));
    }

    private static Object bean(ApplicationContext context, String className) {
        return context.getBean(type(context, className));
    }

    private static List<String> received(ApplicationContext context, String className) {
        return received(bean(context, className));
    }

    @SuppressWarnings("unchecked")
    private static List<String> received(Object listener) {
        try {
            return (List<String>) listener.getClass().getMethod("received").invoke(listener);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot read what the listener received", e);
        }
    }

    private static Class<?> type(ApplicationContext context, String className) {
        try {
            return Class.forName(className, true, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(className + " is not in the application", e);
        }
    }
}
