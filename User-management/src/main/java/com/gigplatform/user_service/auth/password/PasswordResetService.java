package com.gigplatform.user_service.auth.password;

import com.gigplatform.user_service.AuthProvider;
import com.gigplatform.user_service.User;
import com.gigplatform.user_service.UserRepository;
import com.gigplatform.user_service.auth.dto.AuthResponse;
import com.gigplatform.user_service.auth.dto.ForgotPasswordRequest;
import com.gigplatform.user_service.auth.dto.ResetPasswordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String GENERIC_RESET_MESSAGE = "If the account exists, reset instructions were sent.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final JavaMailSender mailSender;

    private final long tokenTtlMinutes;
    private final String baseUrl;
    private final String fromEmail;
    private final String subject;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.password-reset.token-ttl-minutes:30}") long tokenTtlMinutes,
            @Value("${app.password-reset.base-url:http://localhost:8081}") String baseUrl,
            @Value("${app.password-reset.from-email:no-reply@example.com}") String fromEmail,
            @Value("${app.password-reset.subject:Reset your password}") String subject
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.baseUrl = baseUrl;
        this.fromEmail = fromEmail;
        this.subject = subject;
    }

    @Transactional
    public AuthResponse requestReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (user.getProvider() != null && user.getProvider() != AuthProvider.LOCAL) {
                return;
            }
            tokenRepository.deleteByUser_Id(user.getId());
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(generateToken());
            token.setExpiresAt(Instant.now().plus(tokenTtlMinutes, ChronoUnit.MINUTES));
            tokenRepository.save(token);

            String resetLink = baseUrl + "/reset-password.html?token=" + token.getToken();
            sendResetEmail(user.getEmail(), resetLink);
        });

        return new AuthResponse(GENERIC_RESET_MESSAGE);
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        return new AuthResponse("Password updated");
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendResetEmail(String toEmail, String resetLink) {
        String body = "We received a request to reset your password. "
                + "If you did not request this, you can ignore this email.\n\n"
                + "Reset link: " + resetLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send reset email to {}. Link: {}", toEmail, resetLink, ex);
        }
    }
}
