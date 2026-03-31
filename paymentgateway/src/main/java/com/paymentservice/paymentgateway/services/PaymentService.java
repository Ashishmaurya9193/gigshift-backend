package com.paymentservice.paymentgateway.services;

import com.paymentservice.paymentgateway.models.Payment;
import com.paymentservice.paymentgateway.models.PaymentStatus;
import com.paymentservice.paymentgateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private PaymentRepository paymentRepository;

    public String createLink(String orderId) {
        // Logic to create a payment link using Razorpay API


        String paymentLink = paymentGateway.createPaymentLink(orderId, "Ash", "123456789", 10000);

        // Save payment details to the database
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(10000);
        payment.setStatus(PaymentStatus.INITIATED.name());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaymentId("PAY-" + System.currentTimeMillis()); // Generate a unique payment ID
        paymentRepository.save(payment);

        return paymentLink;
    }

    public PaymentStatus getPaymentStatus(String paymentId) {
        // Log the paymentId being checked
        System.out.println("Fetching status for paymentId: " + paymentId);

        // First hit Razorpay and get the status of this payment
        PaymentStatus status = paymentGateway.getStatus(paymentId);
        System.out.println("Status fetched from Razorpay: " + status);

        // Update the payment status in the database
        paymentRepository.findByPaymentId(paymentId).ifPresentOrElse(payment -> {
            System.out.println("Payment found: " + payment);
            payment.setStatus(status.name());
            paymentRepository.save(payment);
            System.out.println("Payment status updated in database: " + payment);
        }, () -> {
            System.out.println("Payment not found for paymentId: " + paymentId);
        });

        return status;
    }
}
