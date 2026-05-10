package com.mmenendez.microservices.notification_microservice.channel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mmenendez.microservices.notification_microservice.customer.CustomerResponse;
import com.mmenendez.microservices.notification_microservice.notification.NotificationRequest;
import com.mmenendez.microservices.notification_microservice.notification.NotificationStatus;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WhatsAppChannel implements NotificationChannel {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String from;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public NotificationStatus send(CustomerResponse customer, NotificationRequest request) {
        if (customer.phone() == null || customer.phone().isBlank()) {
            return NotificationStatus.SKIPPED;
        }

        try {
            String body = buildBody(customer.firstName(), request.orderId(), request.amount(), request.paymentMethod());
            Message.creator(
                new PhoneNumber("whatsapp:" + customer.phone()),
                new PhoneNumber("whatsapp:" + from),
                body
            ).create();
            log.info("WhatsApp message sent to {}", customer.phone());
            return NotificationStatus.SENT;
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", customer.phone(), e.getMessage());
            return NotificationStatus.FAILED;
        }
    }

    private String buildBody(String firstName, Long orderId, Double amount, String paymentMethod) {
        return """
            Hi %s! Your order #%d has been confirmed.
            Total: $%.2f | Payment: %s
            Thank you for your purchase!
            """.formatted(firstName, orderId, amount, paymentMethod);
    }
}
