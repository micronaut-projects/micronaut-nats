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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.constraints.NotNull;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.nats.client.Nats;
import io.nats.client.Options;

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
 * @author jgrimm
 * @since 1.0.0
 */
public abstract class NatsConnectionFactoryConfig {

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

    /**
     * Default constructor.
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
    public void setTls(@NotNull NatsConnectionFactoryConfig.TLS tls) {
        this.tls = tls;
    }

    /**
     * @return NATS options builder based on this set of properties, useful if other settings are required before
     * connect is called
     * @throws IOException if there is a problem reading a file or setting up the SSL context
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
            builder = builder.token(this.token);
        } else if (this.username != null && !this.username.isEmpty()) {
            builder = builder.userInfo(this.username, this.password);
        }

        if (this.tls != null) {
            builder.sslContext(this.tls.createTlsContext());
        }

        return builder;
    }

    /**
     * TLS Configuration.
     */
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
         * @param trustStoreType generally the default, but available for special trust store formats/types
         */
        public void setTrustStoreType(String trustStoreType) {
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

            TrustManagerFactory factory =
                    TrustManagerFactory.getInstance(Optional.ofNullable(trustStoreType).orElse("SunX509"));
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            if (trustStorePath != null && !trustStorePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(trustStorePath))) {
                    ks.load(in, Optional.ofNullable(trustStorePassword).map(String::toCharArray).orElse(new char[0]));
                }
            } else {
                ks.load(null);
            }
            if (certificatePath != null && !certificatePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(certificatePath))) {
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
}
