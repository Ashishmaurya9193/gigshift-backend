package com.gigplatform.notification.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailNotificationDTO {

    @NotNull(message = "Recipient email address cannot be null")
    @Email(message = "Invalid email format")
    private String to;

    private String subject;

    private String text;
}
