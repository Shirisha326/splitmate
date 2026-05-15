package com.splitmate.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
}