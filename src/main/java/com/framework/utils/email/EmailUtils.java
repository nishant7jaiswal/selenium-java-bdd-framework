package com.framework.utils.email;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

/**
 * Email notification utility for post-execution test reports.
 * Sends HTML-formatted reports with optional file attachments.
 *
 * Configured via config.properties:
 *   email.enabled=true
 *   email.recipients=qa-lead@company.com,dev-team@company.com
 *
 * SMTP credentials passed via system properties (Jenkins credentials binding):
 *   -Demail.smtp.host=smtp.gmail.com
 *   -Demail.smtp.user=sender@company.com
 *   -Demail.smtp.password=app-password
 */
public final class EmailUtils {

    private static final Logger log = LogManager.getLogger(EmailUtils.class);

    private static final String SMTP_HOST     = System.getProperty("email.smtp.host",     "smtp.gmail.com");
    private static final String SMTP_PORT     = System.getProperty("email.smtp.port",     "587");
    private static final String SMTP_USER     = System.getProperty("email.smtp.user",     "");
    private static final String SMTP_PASSWORD = System.getProperty("email.smtp.password", "");

    private EmailUtils() {}

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Send a test execution summary email.
     *
     * @param passed   number of passed scenarios
     * @param failed   number of failed scenarios
     * @param skipped  number of skipped scenarios
     * @param reportUrl Allure / Jenkins report URL
     */
    public static void sendExecutionSummary(int passed, int failed, int skipped, String reportUrl) {
        if (!ConfigManager.get().emailEnabled()) {
            log.info("Email notifications disabled — skipping");
            return;
        }

        String subject = buildSubject(passed, failed);
        String body    = buildHtmlBody(passed, failed, skipped, reportUrl);

        sendEmail(subject, body, null);
    }

    /**
     * Send email with an attached file (e.g. CSV report, Allure zip).
     */
    public static void sendWithAttachment(String subject, String htmlBody, String attachmentPath) {
        if (!ConfigManager.get().emailEnabled()) return;
        sendEmail(subject, htmlBody, attachmentPath);
    }

    /**
     * Send a plain failure alert email.
     */
    public static void sendFailureAlert(String scenarioName, String errorMessage) {
        if (!ConfigManager.get().emailEnabled()) return;

        String subject = "❌ Test Failure Alert | " + scenarioName;
        String body = "<h3>Test Failure Detected</h3>"
            + "<p><b>Scenario:</b> " + scenarioName + "</p>"
            + "<p><b>Error:</b></p><pre>" + errorMessage + "</pre>"
            + "<p><b>Time:</b> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")) + "</p>";

        sendEmail(subject, body, null);
    }

    // ── Private ──────────────────────────────────────────────────────────

    private static void sendEmail(String subject, String htmlBody, String attachmentPath) {
        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(SMTP_USER));
            message.setSubject(subject);

            // Recipients
            String recipients = ConfigManager.get().emailRecipients();
            Arrays.stream(recipients.split(","))
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .forEach(recipient -> {
                    try {
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                    } catch (MessagingException e) {
                        log.warn("Invalid recipient: {}", recipient);
                    }
                });

            // Body + optional attachment
            if (attachmentPath != null) {
                MimeMultipart multipart = new MimeMultipart();

                MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(bodyPart);

                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(new File(attachmentPath));
                multipart.addBodyPart(attachPart);

                message.setContent(multipart);
            } else {
                message.setContent(htmlBody, "text/html; charset=utf-8");
            }

            Transport.send(message);
            log.info("Email sent: [{}] → [{}]", subject, recipients);

        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            SMTP_PORT);
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust",       SMTP_HOST);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });
    }

    private static String buildSubject(int passed, int failed) {
        String env    = ConfigManager.get().env().toUpperCase();
        String status = failed == 0 ? "✅ PASSED" : "❌ FAILED";
        String time   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM HH:mm"));
        return String.format("%s | Test Execution Report | %s | %s", status, env, time);
    }

    private static String buildHtmlBody(int passed, int failed, int skipped, String reportUrl) {
        int total      = passed + failed + skipped;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        String statusColor = failed == 0 ? "#2e7d32" : "#c62828";

        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;'>"
            + "<div style='background:" + statusColor + ";color:white;padding:16px;border-radius:4px 4px 0 0;'>"
            + "<h2 style='margin:0;'>" + (failed == 0 ? "✅ All Tests Passed" : "❌ Test Failures Detected") + "</h2>"
            + "</div>"
            + "<div style='border:1px solid #ddd;border-top:none;padding:20px;border-radius:0 0 4px 4px;'>"
            + "<table style='width:100%;border-collapse:collapse;'>"
            + row("Environment",  ConfigManager.get().env().toUpperCase())
            + row("Browser",      ConfigManager.get().browser())
            + row("Total",        String.valueOf(total))
            + row("Passed",       "<span style='color:#2e7d32;font-weight:bold;'>" + passed + "</span>")
            + row("Failed",       "<span style='color:#c62828;font-weight:bold;'>" + failed + "</span>")
            + row("Skipped",      String.valueOf(skipped))
            + row("Pass Rate",    String.format("%.1f%%", passRate))
            + row("Executed At",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")))
            + "</table>"
            + (reportUrl != null
               ? "<p style='margin-top:20px;'><a href='" + reportUrl + "' style='background:#1565c0;color:white;"
                 + "padding:10px 20px;text-decoration:none;border-radius:4px;'>View Allure Report</a></p>"
               : "")
            + "</div></body></html>";
    }

    private static String row(String label, String value) {
        return "<tr style='border-bottom:1px solid #f0f0f0;'>"
            + "<td style='padding:8px;color:#666;width:40%;'>" + label + "</td>"
            + "<td style='padding:8px;font-weight:500;'>" + value + "</td></tr>";
    }
}
