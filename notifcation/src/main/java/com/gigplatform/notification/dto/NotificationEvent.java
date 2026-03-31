package com.gigplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String type; // EMAIL or SMS
    private String to;
    private String subject;
    private String text;
    private String correlationId;
}