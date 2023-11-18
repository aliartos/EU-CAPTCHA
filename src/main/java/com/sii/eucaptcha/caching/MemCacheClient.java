package com.sii.eucaptcha.caching;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MemCacheClient {

    private String configEndpoint = "eucaptchacache.7yiwwr.cfg.euw1.cache.amazonaws.com";
    private Integer clusterPort = 1121;

    private static MemcachedClient client;

    private MemCacheClient() throws IOException {
        client = new MemcachedClient(new InetSocketAddress(configEndpoint,
                clusterPort));
    }

    public static MemcachedClient getInstance() throws IOException {
        if(client == null) {
            new MemCacheClient();
        }
        return client;
    }
}
