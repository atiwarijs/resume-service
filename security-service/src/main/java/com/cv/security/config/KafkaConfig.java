package com.cv.security.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.cv.security.dto.PersonalDetailsDto;


@EnableKafka
@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroupId;

	@Value("${spring.kafka.consumer.auto-offset-reset}")
	private String autoOffsetReset;

	@Value("${spring.kafka.consumer.enable-auto-commit}")
	private boolean enableAutoCommit;

	@Bean
	public ProducerFactory<String, PersonalDetailsDto> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		config.put(ProducerConfig.ACKS_CONFIG, "all");
		config.put(ProducerConfig.RETRIES_CONFIG, 3);
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, PersonalDetailsDto> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

//	@Bean
//	public ConsumerFactory<String, PersonalDetailsDto> consumerFactory() {
//		JsonDeserializer<PersonalDetailsDto> deserializer = new JsonDeserializer<>(PersonalDetailsDto.class);
//		deserializer.addTrustedPackages("*");
//		deserializer.setRemoveTypeHeaders(false);
//		deserializer.setUseTypeMapperForKey(true);
//
//		Map<String, Object> config = new HashMap<>();
//		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//		config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
//		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
//		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
//		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
//
//		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
//	}
//
//	@Bean
//	public ConcurrentKafkaListenerContainerFactory<String, PersonalDetailsDto> kafkaListenerContainerFactory() {
//		ConcurrentKafkaListenerContainerFactory<String, PersonalDetailsDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
//		factory.setConsumerFactory(consumerFactory());
//		factory.setMissingTopicsFatal(false);
//		return factory;
//	}
}
