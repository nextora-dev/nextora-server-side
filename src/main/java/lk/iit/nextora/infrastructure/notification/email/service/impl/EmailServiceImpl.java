package lk.iit.nextora.infrastructure.notification.email.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Email Service Implementation
 *
 * Handles all email sending operations including:
 * - Password reset emails
 * - Account credentials emails
 * - Template loading and caching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    @Value("${app.mail.from:noreply@nextora.lk}")
    private String fromEmail;

    @Value("${app.mail.from-name:Nextora}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

//    @Value("${app.frontend-url:https://nextora.lk}")
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // Cached templates
    private String passwordResetEmailTemplate;
    private String accountCredentialsEmailTemplate;

    @PostConstruct
    public void loadTemplates() {
        try {
            passwordResetEmailTemplate = loadTemplate("classpath:templates/email/password-reset-email.html");
            accountCredentialsEmailTemplate = loadTemplate("classpath:templates/email/account-credentials-email.html");
            log.info("Email templates loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load email templates: {}", e.getMessage());
            throw new RuntimeException("Failed to load email templates", e);
        }
    }

    private String loadTemplate(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }


    @Override
    @Async
    public void sendPasswordResetEmail(BaseUser user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "Reset Your Password - Nextora";
        String htmlContent = buildPasswordResetEmailHtml(user.getFirstName(), resetLink, token);

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
        log.info("Password reset email sent to: {}", maskEmail(user.getEmail()));
    }

    @Override
    @Async
    public void sendAccountCredentialsEmail(String toEmail, String firstName, String loginEmail, String temporaryPassword) {
        String subject = "Your Nextora Account Has Been Created";
        String htmlContent = buildAccountCredentialsEmailHtml(firstName, loginEmail, temporaryPassword);

        sendHtmlEmail(toEmail, subject, htmlContent);
        log.info("Account credentials email sent to: {}", maskEmail(toEmail));
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("Email sent successfully to: {}", maskEmail(to));
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", maskEmail(to), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", maskEmail(to), e.getMessage());
        }
    }

    private String buildPasswordResetEmailHtml(String firstName, String resetLink, String token) {
        return passwordResetEmailTemplate
                .replace("{{firstName}}", firstName)
                .replace("{{resetLink}}", resetLink)
                .replace("{{token}}", token);
    }

    private String buildAccountCredentialsEmailHtml(String firstName, String loginEmail, String temporaryPassword) {
        String loginUrl = frontendUrl + "/login";
        return accountCredentialsEmailTemplate
                .replace("{{firstName}}", firstName)
                .replace("{{loginEmail}}", loginEmail)
                .replace("{{temporaryPassword}}", temporaryPassword)
                .replace("{{loginUrl}}", loginUrl);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex - 1);
    }
}

