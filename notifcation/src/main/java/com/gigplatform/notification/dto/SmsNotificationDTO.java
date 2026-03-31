package com.gigplatform.notification.dto;


import lombok.Data;

@Data
public class SmsNotificationDTO {
    private String to;
    private String text;
}
