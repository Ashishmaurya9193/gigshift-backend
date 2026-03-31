package com.gigplatform.notification.consumer;

import com.gigplatform.notification.dto.NotificationEvent;
import com.gigplatform.notification.service.EmailNotificationService;
import com.gigplatform.notification.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    private final EmailNotificationService emailService;
    private final SmsService smsService;
    private final boolean smsEnabled;

    public NotificationConsumer(EmailNotificationService emailService, SmsService smsService,
                                 @Value("${sms.enabled:false}") boolean smsEnabled) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.smsEnabled = smsEnabled;
    }

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void onMessage(NotificationEvent event) {
        log.info("Received notification event: {}", event);
        if (event == null || event.getType() == null) return;

        try {
            switch (event.getType()) {
                case "EMAIL" -> emailService.sendEmailNotification(
                        event.getTo(), event.getSubject(), event.getText()
                );
                case "SMS" -> {
                    if (smsEnabled) {
                        smsService.sendSms(event.getTo(), event.getText());
                    } else {
                        log.warn("SMS service is disabled. Skipping SMS notification for: {}", event.getTo());
                    }
                }
                default -> throw new IllegalArgumentException("Unknown notification type: " + event.getType());
            }
        } catch (IllegalArgumentException ex) {
            log.error("Error processing notification event: {}", ex.getMessage());
            // Log and skip the message to avoid infinite retries
        } catch (Exception ex) {
            log.error("Unexpected error processing notification event: {}", ex.getMessage(), ex);
            // Send to dead-letter topic or log and skip the message
        }
    }
}
