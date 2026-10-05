package com.cinema.pay_service.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendBookingConfirmationEmail(String toEmail, String subject, String bodyContent, byte[] qrCodeImage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(bodyContent, true);

            if (qrCodeImage != null) {
                helper.addAttachment("ticket-qr.png", new ByteArrayResource(qrCodeImage));
            }

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}