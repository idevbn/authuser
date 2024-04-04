package com.ead.authuser.publishers;

import com.ead.authuser.dtos.UserEventDTO;
import com.ead.authuser.enums.ActionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    @Value("${ead.broker.exchange.userEvent}")
    private String exchangeUserEvent;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UserEventPublisher(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEvent(final UserEventDTO userEventDTO, final ActionType actionType) {
        userEventDTO.setActionType(actionType.toString());
        this.rabbitTemplate.convertAndSend(this.exchangeUserEvent, "", userEventDTO);
    }

}
