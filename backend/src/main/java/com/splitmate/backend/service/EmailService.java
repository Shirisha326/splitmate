package com.splitmate.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendMemberAddedEmail(String toEmail,
            String memberName, String groupName,
            boolean isNewUser) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("You've been added to " + groupName + " on SplitMate! 🎉");

            String body;
            if (isNewUser) {
                body = "Hey " + memberName + "!\n\n" +
                    "You have been added to the group '" + groupName + "' on SplitMate.\n\n" +
                    "Your login details:\n" +
                    "📧 Email: " + toEmail + "\n" +
                    "🔑 Password: Splitmate@123\n" +
                    "🌐 Link: " + baseUrl + "\n\n" +
                    "Login and check your expenses!\n\n" +
                    "SplitMate Team 💸";
            } else {
                body = "Hey " + memberName + "!\n\n" +
                    "You have been added to a new group '" + groupName + "' on SplitMate.\n\n" +
                    "🌐 Login here: " + baseUrl + "\n\n" +
                    "Check your new group and expenses!\n\n" +
                    "SplitMate Team 💸";
            }

            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendExpenseAddedEmail(String toEmail,
            String memberName, String groupName,
            String description, String amount,
            String paidBy, String yourShare) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("New expense added in " + groupName + " 🧾");

            String body = "Hey " + memberName + "!\n\n" +
                "A new expense has been added in '" + groupName + "':\n\n" +
                "💳 Description: " + description + "\n" +
                "💰 Total Amount: ₹" + amount + "\n" +
                "👤 Paid by: " + paidBy + "\n" +
                "📊 Your share: ₹" + yourShare + "\n\n" +
                "🌐 Check it here: " + baseUrl + "\n\n" +
                "SplitMate Team 💸";

            message.setText(body);
            mailSender.send(message);
            log.info("Expense email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send expense email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendSettleUpEmail(String toEmail,
            String memberName, String fromName,
            String amount, String groupName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(fromName + " settled up with you! ✅");

            String body = "Hey " + memberName + "!\n\n" +
                fromName + " has settled ₹" + amount +
                " with you in '" + groupName + "'.\n\n" +
                "🌐 Check it here: " + baseUrl + "\n\n" +
                "SplitMate Team 💸";

            message.setText(body);
            mailSender.send(message);
            log.info("Settle up email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send settle email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Your SplitMate Password Reset Code");

            String html = buildEmail(name,
                "Your password reset code",
                "<p>Use this code to reset your password:</p>" +
                "<div style='background:#1a1612;color:#fff;padding:24px;" +
                "border-radius:12px;text-align:center;margin:20px 0'>" +
                "<p style='font-size:36px;font-weight:bold;letter-spacing:0.3em;" +
                "color:#e8896d;margin:0'>" + otp + "</p>" +
                "</div>" +
                "<p style='color:#999;font-size:13px'>This code expires in 10 minutes." +
                " Do not share it with anyone.</p>",
                "Go to SplitMate", baseUrl
            );

            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmail(String name, String title, String contentHtml,
            String buttonText, String buttonUrl) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;background:#f5f0eb;font-family:Arial,sans-serif'>" +
            "<div style='max-width:520px;margin:40px auto;background:#fff;" +
            "border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08)'>" +

            // Header
            "<div style='background:#1a1612;padding:28px 32px;text-align:center'>" +
            "<h1 style='color:#e8896d;margin:0;font-size:26px;letter-spacing:0.05em'>" +
            "SplitMate 💸</h1>" +
            "</div>" +

            // Body
            "<div style='padding:32px'>" +
            "<p style='font-size:16px;color:#333'>Hey <strong>" + name + "</strong>,</p>" +
            "<h2 style='color:#1a1612;font-size:20px;margin-top:0'>" + title + "</h2>" +
            contentHtml +
            "</div>" +

            // Button
            "<div style='padding:0 32px 24px;text-align:center'>" +
            "<a href='" + buttonUrl + "' style='display:inline-block;background:#e8896d;" +
            "color:#fff;padding:12px 32px;border-radius:8px;text-decoration:none;" +
            "font-weight:bold;font-size:15px'>" + buttonText + "</a>" +
            "</div>" +

            // Footer
            "<div style='background:#f5f0eb;padding:16px 32px;text-align:center'>" +
            "<p style='color:#999;font-size:12px;margin:0'>SplitMate Team &nbsp;|&nbsp;" +
            " This is an automated message, please do not reply.</p>" +
            "</div>" +

            "</div></body></html>";
    }
}