package com.sii.eucaptcha.service;

import net.spy.memcached.MemcachedClient;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ElastiCacheServiceTest {

    private final String configEndpoint = "127.0.0.0";
    private final Integer clusterPort = 11211;

    private MemcachedClient client;

//    @BeforeEach
//    public void setupClient() throws Exception {
//        client = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
//    }

    @Test
    @Ignore
    public void setupTest() throws Exception {
        assertNotNull(client);
        client.set("123456789", 300, "ABCDEF");

        System.out.println(client.get("123456789"));

        assertEquals("ABCDEF", client.get("123456789"));
    }

}
