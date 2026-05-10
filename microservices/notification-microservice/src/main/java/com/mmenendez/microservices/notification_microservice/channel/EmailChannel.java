package com.mmenendez.microservices.notification_microservice.channel;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.mmenendez.microservices.notification_microservice.customer.CustomerResponse;
import com.mmenendez.microservices.notification_microservice.notification.NotificationRequest;
import com.mmenendez.microservices.notification_microservice.notification.NotificationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Override
    public NotificationStatus send(CustomerResponse customer, NotificationRequest request) {
        String subject = "Order #" + request.orderId() + " confirmed";
        String body = buildBody(customer.firstName(), request.orderId(), request.amount(), request.paymentMethod());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customer.email());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", customer.email());
            return NotificationStatus.SENT;
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", customer.email(), e.getMessage());
            return NotificationStatus.FAILED;
        }
    }

    private String buildBody(String firstName, Long orderId, Double amount, String paymentMethod) {
        return """
            Hi %s,

            Your order has been confirmed!

            Order ID:       #%d
            Total amount:   $%.2f
            Payment method: %s

            Thank you for your purchase.

            The E-Commerce Team
            """.formatted(firstName, orderId, amount, paymentMethod);
    }
}
