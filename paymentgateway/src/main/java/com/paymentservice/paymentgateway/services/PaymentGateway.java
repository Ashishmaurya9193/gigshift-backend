package com.paymentservice.paymentgateway.services;

import com.paymentservice.paymentgateway.models.PaymentStatus;
import org.springframework.stereotype.Component;


@Component
public interface PaymentGateway {

    String createPaymentLink(String orderId, String customerName, String phone, int amount);

    PaymentStatus getStatus(String paymentId);
}
