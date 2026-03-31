package com.gigplatform.notification.controller;

import com.gigplatform.notification.dto.EmailNotificationDTO;
import com.gigplatform.notification.dto.SmsNotificationDTO;
import com.gigplatform.notification.service.EmailNotificationService;
import com.gigplatform.notification.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private EmailNotificationService notificationService;

    @Autowired
    private SmsService smsService;
    @PostMapping("/send-email")
    public String sendEmailNotification(@RequestBody EmailNotificationDTO emailNotificationDTO) {
        notificationService.sendEmailNotification(
                emailNotificationDTO.getTo(),
                emailNotificationDTO.getSubject(),
                emailNotificationDTO.getText()
        );
        return "Email notification sent successfully!";
    }

    @PostMapping("/send-sms")
    public String sendSmsNotification(@RequestBody SmsNotificationDTO smsNotificationDTO) {
        smsService.sendSms(
                smsNotificationDTO.getTo(),
                smsNotificationDTO.getText()
        );
        return "SMS notification sent successfully!";
    }
}