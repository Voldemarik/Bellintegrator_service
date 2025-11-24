package ru.bellintegrator.users_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import ru.bellintegrator.users_service.model.UserDto;

import static org.mockito.Mockito.mock;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        public KafkaTemplate<String, UserDto> kafkaTemplate() {
            KafkaTemplate<String, UserDto> kafka = mock(KafkaTemplate.class);
            return kafka;
        }
    }

}
