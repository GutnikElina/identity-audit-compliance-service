package com.innowise.identity_audit_compliance_service.audit.infrastructure.config;

import com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.avro.SystemAuditEventAvro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SystemAuditEventAvro> kafkaListenerContainerFactory(
            ConsumerFactory<String, SystemAuditEventAvro> consumerFactory) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, SystemAuditEventAvro>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}