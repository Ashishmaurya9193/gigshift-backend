package com.paymentservice.paymentgateway.services;


import com.paymentservice.paymentgateway.models.PaymentStatus;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RazorPayGateway implements PaymentGateway{

    private static final Logger logger = LoggerFactory.getLogger(RazorPayGateway.class);


    @Autowired
    private RazorpayClient razorpayClient;

    @Override
    public String createPaymentLink(String orderId, String customerName,
                                    String phone, int amount) {


        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount",amount);
        paymentLinkRequest.put("currency","INR");
        paymentLinkRequest.put("accept_partial",false);
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        long expireBy = currentTimeInSeconds + 1200; // Set expire_by to 20 minutes in the future
        paymentLinkRequest.put("expire_by", expireBy);
        logger.info("Current time in seconds: {}", currentTimeInSeconds);
        logger.info("Expire by timestamp: {}", expireBy);
        paymentLinkRequest.put("reference_id",orderId);
        paymentLinkRequest.put("description","Payment for orderId, "+orderId);

        JSONObject customer = new JSONObject();
        customer.put("name", customerName);
        customer.put("contact", phone);
        customer.put("email", customerName.toLowerCase().replace(" ", ".") + "@example.com");
        paymentLinkRequest.put("customer", customer);

        JSONObject notes = new JSONObject();
        notes.put("policy_name", "Jeevan Bima");

        paymentLinkRequest.put("notes", notes);
        paymentLinkRequest.put("callback_url", "https://scaler.com/");
        paymentLinkRequest.put("callback_method", "get");

        PaymentLink payment = null;
        try {
            payment = razorpayClient.paymentLink.create(paymentLinkRequest);
        } catch (RazorpayException e) {
            logger.error("Error while creating payment link: {}", e.getMessage(), e);
            if (e.getMessage().contains("Recurring digits in customer contact are disallowed")) {
                throw new RuntimeException("Invalid phone number format. Please ensure the phone number does not contain recurring digits.", e);
            }
            throw new RuntimeException("Unable to create the link from Razorpay, try with a different gateway maybe", e);
        }


        return payment.get("short_url");
    }

    @Override
    public PaymentStatus getStatus(String paymentId) {

        Payment payment = null;
        try {
            payment = razorpayClient.payments.fetch(paymentId);
            String status = payment.get("status");
            switch (status) {
                case "captured":
                    return PaymentStatus.SUCCESS;
                case "failed":
                    return PaymentStatus.FAILURE;
                case "created":
                    return PaymentStatus.INITIATED;
                default:
                    throw new IllegalArgumentException("Unknown payment status: " + status);
            }
        } catch (RazorpayException e) {
            throw new RuntimeException("Unable to fetch the payment details", e);
        }
    }
}