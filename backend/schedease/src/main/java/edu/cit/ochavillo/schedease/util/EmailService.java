package edu.cit.ochavillo.schedease.util;

import jakarta.mail.internet.MimeMessage;
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
            String link = "http://localhost:5173/verify?token=" + token;

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

    @Async
    public void sendAppointmentNotification(String to, String clientName, String establishmentName,
                                            String statusOrAction, String date, String time) {
        try {
            Context context = new Context();
            context.setVariable("clientName", clientName);
            context.setVariable("establishmentName", establishmentName);
            context.setVariable("action", statusOrAction); // e.g., "Confirmed", "Cancelled", "Rescheduled"
            context.setVariable("date", date);
            context.setVariable("time", time);

            // You will need to create a new HTML file named 'appointment-notification.html' in your templates folder
            String htmlBody = templateEngine.process("appointment-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Appointment " + statusOrAction + " - " + establishmentName);
            helper.setText(htmlBody, true);
            helper.setFrom("hello@schedease.com");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending appointment notification email: " + e.getMessage());
        }
    }
}
