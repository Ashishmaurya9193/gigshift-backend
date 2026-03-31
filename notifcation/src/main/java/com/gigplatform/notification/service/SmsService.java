package com.gigplatform.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${sms.enabled:false}")
    private boolean enabled;

    public void sendSms(String to, String text) {
        if (!enabled) {
            log.info("SMS service is disabled; ignoring sendSms request to {} with text: {}", to, text);
            return;
        }
        // No-op implementation for now (Twilio removed)
        log.warn("SMS service enabled but no provider is configured. Skipping SMS to {}.", to);
    }
}








//SMS notification implementation removed due to Twilio account issues. The service is left in place with a no-op implementation and logging to allow for future re-enablement without code changes.





/*package com.gigplatform.notification.service;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${sms.enabled:true}")
    private boolean enabled;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    private boolean initialized;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("SMS service disabled; skipping Twilio initialization.");
            return;
        }
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(fromPhoneNumber)) {
            log.warn("SMS service enabled but Twilio config is missing; SMS will be skipped.");
            return;
        }
        Twilio.init(accountSid, authToken);
        initialized = true;
    }

    public void sendSms(String to, String text) {
        if (!enabled) {
            log.info("SMS service disabled; ignoring sendSms request.");
            return;
        }
        if (!initialized) {
            log.warn("SMS service not initialized; skipping sendSms request.");
            return;
        }
        com.twilio.rest.api.v2010.account.Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(fromPhoneNumber),
                text
        ).create();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}*/