package com.innowise.identity_audit_compliance_service.audit.infrastructure.config;

import com.innowise.identity_audit_compliance_service.audit.infrastructure.kafka.avro.SystemAuditEventAvro;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, SystemAuditEventAvro> consumerFactory(
      KafkaProperties kafkaProperties) {
    return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SystemAuditEventAvro>
      kafkaListenerContainerFactory(ConsumerFactory<String, SystemAuditEventAvro> consumerFactory) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, SystemAuditEventAvro>();
    factory.setConsumerFactory(consumerFactory);
    factory.setBatchListener(true);

    var executor = new SimpleAsyncTaskExecutor("kafka-vt-");
    executor.setVirtualThreads(true);

    var containerProperties = factory.getContainerProperties();
    containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    containerProperties.setListenerTaskExecutor(executor);

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3));

    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }
}
