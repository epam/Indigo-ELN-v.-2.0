package com.epam.indigoeln;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Dummy JMS message receiver. Required for automatic JMS initialization.
 * TODO: should be deleted after normal JMS receivers will be commited.
 */
@Component
public class TestComponentMessageReceiver {

    @JmsListener(destination = "ping")
    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
    }

}
