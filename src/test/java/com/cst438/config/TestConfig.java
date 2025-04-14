package com.cst438.config;

import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.RegistrarServiceProxy;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @MockBean
    RabbitTemplate rabbitTemplate;

    @MockBean
    ConnectionFactory connectionFactory;

    @Bean
    @Primary
    public RegistrarServiceProxyTest registrarServiceProxyTest() {
        return new RegistrarServiceProxyTest();
    }

    @Bean
    public Queue gradebookServiceQueue() {
        return new Queue("gradebook_service_test", false);
    }

    public static class RegistrarServiceProxyTest extends RegistrarServiceProxy {
        private List<String> messagesSent = new ArrayList<>();
        private int messageCount = 0;
        private String lastMessageContent = null;

        public void reset() {
            messagesSent.clear();
            messageCount = 0;
            lastMessageContent = null;
        }

        @Override
        public void sendFinalGrade(EnrollmentDTO enrollmentDTO) {
            messageCount++;
            String content = "updateEnrollmentGrade " + enrollmentDTO.toString();
            lastMessageContent = content;
            messagesSent.add(content);
        }

        @Override
        public Queue createQueue() {
            return new Queue("gradebook_service_test", false);
        }

        @Override
        public void receiveFromRegistrar(String message) {
            // Override to prevent actual RabbitMQ listener behavior
        }

        public int getMessageCount() {
            return messageCount;
        }

        public String getLastMessageContent() {
            return lastMessageContent;
        }

        public List<String> getMessagesSent() {
            return messagesSent;
        }
    }
} 