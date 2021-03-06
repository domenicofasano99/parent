package com.bok.parent.be.messaging;

import com.bok.parent.integration.message.AccountClosureMessage;
import com.bok.parent.integration.message.AccountCreationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KryptoUserMessageProducer {

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${queues.krypto-users}")
    private String usersQueue;

    @Value("${queues.krypto-account-deletion}")
    private String accountDeletionQueue;


    public void send(AccountCreationMessage userCreationMessage) {
        try {
            log.info("Attempting send account creation  to queue: " + usersQueue);
            jmsTemplate.convertAndSend(usersQueue, userCreationMessage);
        } catch (Exception e) {
            log.error("Received Exception during send Message: ", e);
        }
    }


    public void send(AccountClosureMessage accountClosureMessage) {
        try {
            log.info("Attempting send account deletion to queue: " + accountDeletionQueue);
            jmsTemplate.convertAndSend(accountDeletionQueue, accountClosureMessage);
        } catch (Exception e) {
            log.error("Received Exception during send Message: ", e);
        }
    }
}
