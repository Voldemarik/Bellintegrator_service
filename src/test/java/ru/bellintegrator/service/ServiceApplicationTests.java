package ru.bellintegrator.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@SpringBootTest
class ServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        public KafkaTemplate kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }

}
