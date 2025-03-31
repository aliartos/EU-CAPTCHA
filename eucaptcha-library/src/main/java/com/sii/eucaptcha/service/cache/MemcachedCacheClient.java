package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.config.NodeEndPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * Memcached implementation of the CacheClient interface.
 * Uses AWS ElastiCache Memcached client.
 */
@Slf4j
@Component
public class MemcachedCacheClient implements CacheClient {

    private MemcachedClient client;
    
    @Value("${cache.memcached.host:AWS host and port}")
    private String memcachedHost;
    
    @Value("${cache.memcached.hostname:AWS hostname}")
    private String memcachedHostname;

    @Override
    public void init() {
        try {
            ConnectionFactoryBuilder connectionFactoryBuilder = new ConnectionFactoryBuilder();
            // Build SSLContext
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            // Create the client in TLS mode
            connectionFactoryBuilder.setSSLContext(sslContext);
            connectionFactoryBuilder.setClientMode(ClientMode.Dynamic);
            // TLS mode enables hostname verification by default. It is always recommended to do that.
            connectionFactoryBuilder.setHostnameForTlsVerification(memcachedHostname);
            client = new MemcachedClient(
                    connectionFactoryBuilder.build(), AddrUtil.getAddresses(memcachedHost));
            Collection<NodeEndPoint> endpoints = client.getAllNodeEndPoints();
            for (NodeEndPoint endPoint : endpoints) {
                log.info("Available endpoint {} with port {}", endPoint.getHostName(), endPoint.getPort());
                log.info("Connection is active {}", client.isConfigurationInitialized());
            }
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("Failed to initialize Memcached client", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String key, int expiry, Object value) {
        if (client != null) {
            client.set(key, expiry, value);
            log.debug("Added key {} with value {} in the cache", key, value);
        } else {
            log.error("Memcached client is not initialized");
        }
    }

    @Override
    public Object get(String key) {
        if (client != null) {
            Object value = client.get(key);
            log.debug("Retrieved key {} with value {} from the cache", key, value);
            return value;
        } else {
            log.error("Memcached client is not initialized");
            return null;
        }
    }

    @Override
    public void delete(String key) {
        if (client != null) {
            client.delete(key);
            log.debug("Deleted key {} from the cache", key);
        } else {
            log.error("Memcached client is not initialized");
        }
    }

    @Override
    public boolean isInitialized() {
        return client != null && client.isConfigurationInitialized();
    }
}