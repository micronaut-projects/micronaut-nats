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
package io.micronaut.nats.connect;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Nullable;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;
import io.nats.client.Options;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.nats.client.Options.Builder;
import static io.nats.client.Options.DEFAULT_INBOX_PREFIX;
import static io.nats.client.Options.DEFAULT_MAX_RECONNECT;
import static io.nats.client.Options.DEFAULT_PING_INTERVAL;
import static io.nats.client.Options.DEFAULT_RECONNECT_BUF_SIZE;
import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;
import static io.nats.client.Options.DEFAULT_SSL_PROTOCOL;
import static io.nats.client.Options.DEFAULT_URL;

/**
 * Base class for nats to be configured.
 *
 * @author jgrimm
 * @since 1.0.0
 */
@Requires(property = NatsConnectionFactoryConfig.PREFIX)
@EachProperty(value = NatsConnectionFactoryConfig.PREFIX, primary = "default")
public class NatsConnectionFactoryConfig {

    public static final String PREFIX = "nats";

    private final String name;

    private List<String> addresses = Collections.singletonList(DEFAULT_URL);

    private int maxReconnect = DEFAULT_MAX_RECONNECT;

    private Duration reconnectWait = DEFAULT_RECONNECT_WAIT;

    private Duration connectionTimeout = Options.DEFAULT_CONNECTION_TIMEOUT;

    private Duration pingInterval = DEFAULT_PING_INTERVAL;

    private long reconnectBufferSize = DEFAULT_RECONNECT_BUF_SIZE;

    private String inboxPrefix = DEFAULT_INBOX_PREFIX;

    private boolean noEcho;

    private boolean utf8Support;

    private String username;

    private String password;

    private String token;

    private String credentials;

    private TLS tls;

    @Nullable
    private JetStreamConfiguration jetstream;

    /**
     * Default constructor.
     *
     * @param name The name of the configuration
     */
    public NatsConnectionFactoryConfig(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name qualifier
     */
    public String getName() {
        return name;
    }

    /**
     * @return An optional list of addresses
     */
    public Optional<List<String>> getAddresses() {
        return Optional.ofNullable(addresses);
    }

    /**
     * @param addresses The list of addresses
     */
    public void setAddresses(@Nullable List<String> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the username for the connection
     */
    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    /**
     * @param username the username
     */
    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    /**
     * @return the password for the connection
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    /**
     * @param password the password
     */
    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    /**
     * @return the token for the connection
     */
    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    /**
     * @param token the token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the max reconnection tries
     */
    public int getMaxReconnect() {
        return maxReconnect;
    }

    /**
     * @param maxReconnect times to try reconnect
     */
    public void setMaxReconnect(int maxReconnect) {
        this.maxReconnect = maxReconnect;
    }

    /**
     * @return time to wait between reconnect attempts
     */
    public Duration getReconnectWait() {
        return reconnectWait;
    }

    /**
     * @param reconnectWait time to wait
     */
    public void setReconnectWait(Duration reconnectWait) {
        this.reconnectWait = reconnectWait;
    }

    /**
     * @return maximum time for initial connection
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout maximumTime for inital connection
     */
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @return time between ping intervals
     */
    public Duration getPingInterval() {
        return pingInterval;
    }

    /**
     * @param pingInterval time between server pings
     */
    public void setPingInterval(Duration pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * @return size of the buffer, in bytes, used to store publish messages during reconnect
     */
    public long getReconnectBufferSize() {
        return reconnectBufferSize;
    }

    /**
     * @param reconnectBufferSize size of the buffer, in bytes, used to store publish messages during reconnect
     */
    public void setReconnectBufferSize(long reconnectBufferSize) {
        this.reconnectBufferSize = reconnectBufferSize;
    }

    /**
     * @return prefix to use for request/reply inboxes
     */
    public String getInboxPrefix() {
        return inboxPrefix;
    }

    /**
     * @param inboxPrefix custom prefix for request/reply inboxes
     */
    public void setInboxPrefix(String inboxPrefix) {
        this.inboxPrefix = inboxPrefix;
    }

    /**
     * @return whether or not to block echo messages, messages that were sent by this connection
     */
    public boolean isNoEcho() {
        return noEcho;
    }

    /**
     * @param noEcho enable or disable echo messages, messages that are sent by this connection back to this connection
     */
    public void setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
    }

    /**
     * @return whether or not the client should support for UTF8 subject names
     */
    public boolean isUtf8Support() {
        return utf8Support;
    }

    /**
     * @param utf8Support whether or not the client should support for UTF8 subject names
     */
    public void setUtf8Support(boolean utf8Support) {
        this.utf8Support = utf8Support;
    }

    /**
     * @return path to the credentials file to use for authentication with an account enabled server
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * @param credentials path to the credentials file to use for authentication with an account enabled server
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /**
     * @param tls The tls configuration
     */
    public void setTls(@Nullable NatsConnectionFactoryConfig.TLS tls) {
        this.tls = tls;
    }

    /**
     * @return NATS options builder based on this set of properties, useful if other settings are required before
     * connect is called
     * @throws IOException              if there is a problem reading a file or setting up the SSL context
     * @throws GeneralSecurityException if there is a problem setting up the SSL context
     */
    public Builder toOptionsBuilder() throws IOException, GeneralSecurityException {
        Builder builder = new Builder();

        builder = builder.servers(this.addresses.toArray(new String[0]));
        builder = builder.maxReconnects(this.maxReconnect);
        builder = builder.reconnectWait(this.reconnectWait);
        builder = builder.connectionTimeout(this.connectionTimeout);
        builder = builder.connectionName(this.name);
        builder = builder.pingInterval(this.pingInterval);
        builder = builder.reconnectBufferSize(this.reconnectBufferSize);
        builder = builder.inboxPrefix(this.inboxPrefix);

        if (this.noEcho) {
            builder = builder.noEcho();
        }

        if (this.utf8Support) {
            builder = builder.supportUTF8Subjects();
        }

        if (this.credentials != null && !this.credentials.isEmpty()) {
            builder = builder.authHandler(Nats.credentials(this.credentials));
        } else if (this.token != null && !this.token.isEmpty()) {
            builder = builder.token(this.token.toCharArray());
        } else if (this.username != null && !this.username.isEmpty()) {
            builder = builder.userInfo(this.username, this.password);
        }

        if (this.tls != null) {
            builder.sslContext(this.tls.createTlsContext());
        }

        return builder;
    }

    /**
     * get the optional jetstream configuration.
     *
     * @return the jetstream configuration
     */
    @Nullable
    public JetStreamConfiguration getJetstream() {
        return jetstream;
    }

    /**
     * @param jetstream the jestream configuration
     */
    @ConfigurationProperties("jetstream")
    public void setJetstream(@Nullable JetStreamConfiguration jetstream) {
        this.jetstream = jetstream;
    }

    /**
     * TLS Configuration.
     */
    @ConfigurationProperties("tls")
    public static class TLS {

        private String trustStorePath;

        private String trustStorePassword;

        private String trustStoreType;

        private String certificatePath;

        /**
         * @return file path for the trust store
         */
        public String getTrustStorePath() {
            return this.trustStorePath;
        }

        /**
         * @param trustStorePath file path for the trust store
         */
        public void setTrustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
        }

        /**
         * @return password used to unlock the trust store
         */
        public String getTrustStorePassword() {
            return this.trustStorePassword;
        }

        /**
         * @param trustStorePassword used to unlock the trust store
         */
        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        /**
         * @return type of keystore to use for connections
         */
        public String getTrustStoreType() {
            return this.trustStoreType;
        }

        /**
         * @param trustStoreType generally the default, but available for special trust store
         *                       formats/types
         */
        public void setTrustStoreType(@Nullable String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        /**
         * @return the certificate path
         */
        public String getCertificatePath() {
            return certificatePath;
        }

        /**
         * @param certificatePath the path to the certificate
         */
        public void setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
        }

        private SSLContext createTlsContext() throws IOException, GeneralSecurityException {
            SSLContext ctx = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);

            TrustManagerFactory factory = TrustManagerFactory.getInstance(Optional.ofNullable(trustStoreType).orElse("SunX509"));
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            if (trustStorePath != null && !trustStorePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(trustStorePath)))) {
                    ks.load(in, Optional.ofNullable(trustStorePassword).map(String::toCharArray).orElse(new char[0]));
                }
            } else {
                ks.load(null);
            }
            if (certificatePath != null && !certificatePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(certificatePath)))) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
                    ks.setCertificateEntry("nats", cert);
                }
            }
            factory.init(ks);
            ctx.init(null, factory.getTrustManagers(), new SecureRandom());

            return ctx;
        }

    }

    /**
     * Manages the jetstream configuration.
     *
     * @author Joachim Grimm
     * @since 4.0.0
     */
    @ConfigurationProperties("jetstream")
    public static class JetStreamConfiguration {

        @ConfigurationBuilder(prefixes = "", excludes = {"build"})
        private JetStreamOptions.Builder builder = JetStreamOptions.builder(JetStreamOptions.defaultOptions());

        private List<StreamConfiguration> streams = new ArrayList<>();

        private List<KeyValueConfiguration> keyvalue = new ArrayList<>();

        private List<ObjectStoreConfiguration> objectstore = new ArrayList<>();

        /**
         * get the jetstream options builder.
         *
         * @return options builder
         */
        public JetStreamOptions.Builder getBuilder() {
            return builder;
        }

        /**
         * return the configuration as {@link JetStreamOptions}.
         *
         * @return jetstream options
         */
        public JetStreamOptions toJetStreamOptions() {
            return builder.build();
        }

        /**
         * get the stream configurations.
         *
         * @return list of streamConfigurations
         */
        public List<StreamConfiguration> getStreams() {
            return streams;
        }

        /**
         * set the stream configurations for the jetstream.
         *
         * @param streams the stream configurations
         */
        public void setStreams(List<StreamConfiguration> streams) {
            this.streams = streams;
        }

        /**
         * get the key value configurations.
         *
         * @return list of key value configurations
         */
        public List<KeyValueConfiguration> getKeyvalue() {
            return keyvalue;
        }

        /**
         * set the keyvalue configurations.
         *
         * @param keyvalue list of key value configurations
         */
        public void setKeyvalue(List<KeyValueConfiguration> keyvalue) {
            this.keyvalue = keyvalue;
        }

        /**
         * get the object store configurations.
         *
         * @return list of object store configurations
         */
        public List<ObjectStoreConfiguration> getObjectstore() {
            return objectstore;
        }

        /**
         * set the object store configurations.
         *
         * @param objectstore list of object store configurations
         */
        public void setObjectstore(List<ObjectStoreConfiguration> objectstore) {
            this.objectstore = objectstore;
        }

        /**
         * Manages a single stream configuration.
         */
        @EachProperty(value = "streams")
        public static class StreamConfiguration {

            private final String name;
            @ConfigurationBuilder(prefixes = "", excludes = {"addSubjects", "addSources", "addSource", "name", "subjects", "build", "placement", "subjectTransform", "republish", "mirror", "sources", "consumerLimits"})
            private io.nats.client.api.StreamConfiguration.Builder builder = io.nats.client.api.StreamConfiguration.builder();
            private List<String> subjects;

            private Placement placement;

            private SubjectTransform subjectTransform;

            private Mirror mirror;

            private List<Source> sources = new ArrayList<>();

            private Republish republish;

            private ConsumerLimits consumerLimits;

            public StreamConfiguration(@Parameter String name) {
                this.name = name;
            }

            /**
             * get the stream configuration builder.
             *
             * @return stream configuration builder
             */
            public io.nats.client.api.StreamConfiguration.Builder getBuilder() {
                return builder;
            }

            /**
             * return the configuration as
             * {@link io.nats.client.api.StreamConfiguration}.
             *
             * @return nats stream configuration
             */
            public io.nats.client.api.StreamConfiguration toStreamConfiguration() {
                io.nats.client.api.StreamConfiguration.Builder streamBuilder = builder.name(name)
                    .subjects(subjects)
                    .sources(sources.stream().map(io.micronaut.nats.connect.Source::build).toList());

                if (mirror != null) {
                    streamBuilder = streamBuilder.mirror(mirror.build());
                }
                if (republish != null) {
                    streamBuilder = streamBuilder.republish(republish.build());
                }
                if (consumerLimits != null) {
                    streamBuilder = streamBuilder.consumerLimits(consumerLimits.build());
                }
                if (placement != null) {
                    streamBuilder = streamBuilder.placement(placement.build());
                }
                if (subjectTransform != null) {
                    streamBuilder = streamBuilder.subjectTransform(subjectTransform.build());
                }
                return streamBuilder.build();
            }

            /**
             * get the subjects of the stream.
             *
             * @return the subjects
             */
            public List<String> getSubjects() {
                return subjects;
            }

            /**
             * set the subjects.
             *
             * @param subjects list of subjects
             */
            public void setSubjects(List<String> subjects) {
                this.subjects = subjects;
            }

            /**
             * the Placement.
             *
             * @return placement
             */
            public Placement getPlacement() {
                return placement;
            }

            /**
             * the Placement.
             *
             * @param placement {@link Placement}
             */
            public void setPlacement(Placement placement) {
                this.placement = placement;
            }

            /**
             * The Subject Transform.
             *
             * @return subject transform
             */
            public SubjectTransform getSubjectTransform() {
                return subjectTransform;
            }

            /**
             * The Subject Transform.
             *
             * @param subjectTransform SubjectTransform
             */
            public void setSubjectTransform(SubjectTransform subjectTransform) {
                this.subjectTransform = subjectTransform;
            }

            /**
             * The mirror.
             *
             * @return mirror
             */
            public Mirror getMirror() {
                return mirror;
            }

            /**
             * The mirror.
             *
             * @param mirror {@link Mirror}
             */
            public void setMirror(Mirror mirror) {
                this.mirror = mirror;
            }

            /**
             * sources.
             *
             * @return list of sources
             */
            public List<Source> getSources() {
                return sources;
            }

            /**
             * sources.
             *
             * @param sources list of sources
             */
            public void setSources(List<Source> sources) {
                this.sources = sources;
            }

            /**
             * Republish.
             *
             * @return republish
             */
            public Republish getRepublish() {
                return republish;
            }

            /**
             * Republish.
             *
             * @param republish {@link Republish}
             */
            public void setRepublish(Republish republish) {
                this.republish = republish;
            }

            /**
             * Consumer Limits.
             *
             * @return the limits
             */
            public ConsumerLimits getConsumerLimits() {
                return consumerLimits;
            }

            /**
             * Consumer Limits.
             * @param consumerLimits {@link ConsumerLimits}
             */
            public void setConsumerLimits(ConsumerLimits consumerLimits) {
                this.consumerLimits = consumerLimits;
            }

            /**
             * Placement.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("placement")
            public static class Placement extends io.micronaut.nats.connect.Placement {
            }

            /**
             * Subject Transform.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("subject-transform")
            public static class SubjectTransform extends SubjectTransformBase {
            }

            /**
             * Republish.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("republish")
            public static class Republish extends io.micronaut.nats.connect.Republish {
            }

            /**
             * Republish.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("consumer-limits")
            public static class ConsumerLimits extends io.micronaut.nats.connect.ConsumerLimits {
            }

            /**
             * Mirror.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("mirror")
            public static class Mirror extends io.micronaut.nats.connect.Mirror<SubjectTransformBase, Mirror.External> {

                /**
                 * Subject transformations.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @EachProperty(value = "subject-transforms", list = true)
                public static class SubjectTransform extends SubjectTransformBase {

                }

                /**
                 * External.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @ConfigurationProperties("external")
                public static class External extends SourceBase.External {

                }
            }

            /**
             * Source.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @EachProperty(value = "sources", list = true)
            public static class Source extends io.micronaut.nats.connect.Source<SubjectTransform, Mirror.External> {

                /**
                 * Subject transformations.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @EachProperty(value = "subject-transforms", list = true)
                public static class SubjectTransform extends SubjectTransformBase {

                }

                /**
                 * External.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @ConfigurationProperties("external")
                public static class External extends SourceBase.External {

                }
            }

        }


        /**
         * Manages a single key value configuration.
         */
        @EachProperty(value = "keyvalue")
        public static class KeyValueConfiguration {

            private final String name;

            private Placement placement;

            private Mirror mirror;

            private List<Source> sources = new ArrayList<>();

            @ConfigurationBuilder(prefixes = "", excludes = {"addSources", "addSource", "name", "sources", "build", "placement", "republish", "mirror"})
            private io.nats.client.api.KeyValueConfiguration.Builder builder = io.nats.client.api.KeyValueConfiguration.builder();

            private Republish republish;

            public KeyValueConfiguration(@Parameter String name) {
                this.name = name;
            }

            /**
             * get the key value configuration builder.
             *
             * @return key value configuration builder
             */
            public io.nats.client.api.KeyValueConfiguration.Builder getBuilder() {
                return builder;
            }

            /**
             * return the configuration as
             * {@link io.nats.client.api.KeyValueConfiguration}.
             *
             * @return nats key value configuration
             */
            public io.nats.client.api.KeyValueConfiguration toKeyValueConfiguration() {
                io.nats.client.api.KeyValueConfiguration.Builder keyValueBuilder = builder
                    .name(name)
                    .sources(sources.stream().map(io.micronaut.nats.connect.Source::build).toList());
                if (mirror != null) {
                    keyValueBuilder = keyValueBuilder.mirror(mirror.build());
                }
                if (republish != null) {
                    keyValueBuilder = keyValueBuilder.republish(republish.build());
                }
                if (placement != null) {
                    keyValueBuilder = keyValueBuilder.placement(placement.build());
                }
                return keyValueBuilder.build();
            }

            /**
             * the Placement.
             *
             * @return placement
             */
            public Placement getPlacement() {
                return placement;
            }

            /**
             * the Placement.
             *
             * @param placement {@link Placement}
             */
            public void setPlacement(Placement placement) {
                this.placement = placement;
            }

            /**
             * The mirror.
             *
             * @return mirror
             */
            public Mirror getMirror() {
                return mirror;
            }

            /**
             * The mirror.
             *
             * @param mirror Mirror
             */
            public void setMirror(Mirror mirror) {
                this.mirror = mirror;
            }

            /**
             * sources.
             *
             * @return list of sources
             */
            public List<Source> getSources() {
                return sources;
            }

            /**
             * sources.
             *
             * @param sources list of sources
             */
            public void setSources(List<Source> sources) {
                this.sources = sources;
            }

            /**
             * Republish.
             *
             * @return republish
             */
            public Republish getRepublish() {
                return republish;
            }

            /**
             * Republish.
             *
             * @param republish {@link Republish}
             */
            public void setRepublish(Republish republish) {
                this.republish = republish;
            }

            /**
             * Placement.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("placement")
            public static class Placement extends io.micronaut.nats.connect.Placement {
            }

            /**
             * Republish.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("republish")
            public static class Republish extends io.micronaut.nats.connect.Republish {
            }

            /**
             * Mirror.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("mirror")
            public static class Mirror extends io.micronaut.nats.connect.Mirror<SubjectTransformBase, Mirror.External> {

                /**
                 * Subject transformations.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @EachProperty(value = "subject-transforms", list = true)
                public static class SubjectTransform extends SubjectTransformBase {

                }

                /**
                 * External.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @ConfigurationProperties("external")
                public static class External extends SourceBase.External {

                }
            }

            /**
             * Sources.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @EachProperty(value = "sources", list = true)
            public static class Source extends io.micronaut.nats.connect.Source<Source.SubjectTransform, Source.External> {

                /**
                 * Subject transformations.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @EachProperty(value = "subject-transforms", list = true)
                public static class SubjectTransform extends SubjectTransformBase {

                }

                /**
                 * External.
                 *
                 * @author Joachim Grimm
                 * @since 4.1.0
                 */
                @ConfigurationProperties("external")
                public static class External extends SourceBase.External {

                }
            }
        }

        /**
         * Manages a single object store configuration.
         */
        @EachProperty(value = "objectstore")
        @Experimental
        public static class ObjectStoreConfiguration {

            private final String name;

            private Placement placement;

            @ConfigurationBuilder(prefixes = "", excludes = {"name", "build", "placement"})
            private io.nats.client.api.ObjectStoreConfiguration.Builder builder = io.nats.client.api.ObjectStoreConfiguration.builder();

            public ObjectStoreConfiguration(@Parameter String name) {
                this.name = name;
            }

            /**
             * get the object store configuration builder.
             *
             * @return object store configuration builder
             */
            public io.nats.client.api.ObjectStoreConfiguration.Builder getBuilder() {
                return builder;
            }

            /**
             * the Placement.
             *
             * @return placement
             */
            public Placement getPlacement() {
                return placement;
            }

            /**
             * the Placement.
             *
             * @param placement {@link Placement}
             */
            public void setPlacement(Placement placement) {
                this.placement = placement;
            }

            /**
             * return the configuration as
             * {@link io.nats.client.api.ObjectStoreConfiguration}.
             *
             * @return nats object store configuration
             */
            public io.nats.client.api.ObjectStoreConfiguration toObjectStoreConfiguration() {
                io.nats.client.api.ObjectStoreConfiguration.Builder objectStoreBuilder = builder.name(name);
                if (placement != null) {
                    objectStoreBuilder = objectStoreBuilder.placement(placement.build());
                }
                return objectStoreBuilder.build();
            }

            /**
             * Placement.
             *
             * @author Joachim Grimm
             * @since 4.1.0
             */
            @ConfigurationProperties("placement")
            public static class Placement extends io.micronaut.nats.connect.Placement {
            }
        }
    }
}
