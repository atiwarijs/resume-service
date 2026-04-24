package com.cv.security.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.cv.security.dto.PersonalDetailsDto;
import com.cv.security.dto.UserDTO;


@Component
public class UserEventPublisher {

	private final KafkaTemplate<String, PersonalDetailsDto> kafkaTemplate;

	public UserEventPublisher(KafkaTemplate<String, PersonalDetailsDto> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishUserCreated(PersonalDetailsDto userDTO) {
		kafkaTemplate.send("user.created", userDTO);
	}
	
	public void publishUserUpdated(PersonalDetailsDto userDTO) {
		kafkaTemplate.send("user.updated", userDTO);
	}
}
