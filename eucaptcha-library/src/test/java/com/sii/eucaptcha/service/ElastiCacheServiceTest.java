package com.sii.eucaptcha.service;
import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@ActiveProfiles("test")
public class ElastiCacheServiceTest {

    private final String configEndpoint = "127.0.0.0";
    private final Integer clusterPort = 11211;

    private MemcachedClient client;

//    @BeforeEach
//    public void setupClient() throws Exception {
//        client = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
//    }

    @Test
    @Disabled
    public void setupTest() throws Exception {
        assertNotNull(client);
        client.set("123456789", 300, "ABCDEF");

        System.out.println(client.get("123456789"));

        assertEquals("ABCDEF", client.get("123456789"));
    }

}
