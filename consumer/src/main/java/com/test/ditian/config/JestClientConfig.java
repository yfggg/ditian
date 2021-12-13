package com.test.ditian.config;

import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * springboot2.3.0之后不支持自动注册，只能手动写注册配置文件.
 */
@Component
public class JestClientConfig {

    @Bean
    public io.searchbox.client.JestClient getJestCline() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(2)
                .maxTotalConnection(10)
                .build());
        return factory.getObject();
    }

}
