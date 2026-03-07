package edu.cit.ochavillo.schedease.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            // Build the link (Update this to your actual production domain later!)
            String link = "http://localhost:8080/api/auth/verify?token=" + token;

            // Prepare context data for Thymeleaf
            Context context = new Context();
            context.setVariable("verificationUrl", link);

            // Generate HTML string from template
            String htmlBody = templateEngine.process("verification-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Verify your SchedEase account");
            helper.setText(htmlBody, true); // Important: 'true' tells Spring this is HTML
            helper.setFrom("hello@schedease.com");

            mailSender.send(message);
        } catch (Exception e) {
            // Use a proper logger here
            System.err.println("Error sending verification email: " + e.getMessage());
        }
    }
}
