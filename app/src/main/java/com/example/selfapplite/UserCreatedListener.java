package com.example.selfapplite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedListener.class);

    @RabbitListener(queues = RabbitConfig.USER_CREATED_QUEUE)
    public void onUserCreated(String message) {
        log.info("[{}] received: {}", RabbitConfig.USER_CREATED_QUEUE, message);
    }
}
