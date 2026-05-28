package com.splitmate.backend.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${mailjet.api-key}")
    private String apiKey;

    @Value("${mailjet.secret-key}")
    private String secretKey;

    @Value("${mailjet.from-email}")
    private String fromEmail;

    @Value("${mailjet.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    private void sendEmail(String toEmail, String toName,
            String subject, String htmlBody) {
        try {
            MailjetClient client = new MailjetClient(
                ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(secretKey)
                    .build()
            );

            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                    .put(new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", fromEmail)
                            .put("Name", fromName))
                        .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                .put("Email", toEmail)
                                .put("Name", toName)))
                        .put(Emailv31.Message.SUBJECT, subject)
                        .put(Emailv31.Message.HTMLPART, htmlBody)
                    )
                );

            MailjetResponse response = client.post(request);
            log.info("Email sent to {} - Status: {}",
                toEmail, response.getStatus());

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}",
                toEmail, e.getMessage());
        }
    }

    @Async
    public void sendMemberAddedEmail(String toEmail,
            String memberName, String groupName,
            boolean isNewUser) {
        String subject = "🎉 You've been added to "
            + groupName + " on SplitMate!";
        String content;

        if (isNewUser) {
            content =
                "<p>Here are your login details:</p>" +
                "<div style='background:#f5f5f5;padding:16px;" +
                "border-radius:8px;margin:16px 0'>" +
                "<p>📧 <strong>Email:</strong> " + toEmail + "</p>" +
                "<p>🔑 <strong>Password:</strong> Splitmate@123</p>" +
                "</div>" +
                "<p>Please change your password after first login.</p>";
        } else {
            content =
                "<p>You have been added to <strong>" +
                groupName + "</strong>.</p>" +
                "<p>Login to see your group and expenses.</p>";
        }

        sendEmail(toEmail, memberName, subject,
            buildEmail(memberName,
                "You've been added to <strong>" +
                groupName + "</strong>!",
                content, "Login to SplitMate", baseUrl));
    }

    @Async
    public void sendExpenseAddedEmail(String toEmail,
            String memberName, String groupName,
            String description, String amount,
            String paidBy, String yourShare) {
        String subject = "🧾 New expense in " + groupName;
        String content =
            "<div style='background:#f5f5f5;padding:16px;" +
            "border-radius:8px;margin:16px 0'>" +
            "<p>💳 <strong>Description:</strong> " + description + "</p>" +
            "<p>💰 <strong>Total Amount:</strong> ₹" + amount + "</p>" +
            "<p>👤 <strong>Paid by:</strong> " + paidBy + "</p>" +
            "<p style='color:#c9623f;font-size:18px'>" +
            "📊 <strong>Your share: ₹" + yourShare + "</strong></p>" +
            "</div>";

        sendEmail(toEmail, memberName, subject,
            buildEmail(memberName,
                "New expense in <strong>" + groupName + "</strong>",
                content, "View Expense", baseUrl));
    }

    @Async
    public void sendSettleUpEmail(String toEmail,
            String memberName, String fromUserName,
            String amount, String groupName) {
        String subject = "✅ " + fromUserName + " settled up with you!";
        String content =
            "<div style='background:#f5f5f5;padding:16px;" +
            "border-radius:8px;margin:16px 0'>" +
            "<p>✅ <strong>" + fromUserName + "</strong> paid you</p>" +
            "<p style='color:#4a7c59;font-size:24px;" +
            "font-weight:bold'>₹" + amount + "</p>" +
            "<p>Group: <strong>" + groupName + "</strong></p>" +
            "</div>";

        sendEmail(toEmail, memberName, subject,
            buildEmail(memberName,
                fromUserName + " settled up with you!",
                content, "View Settlement", baseUrl));
    }

    @Async
    public void sendOtpEmail(String toEmail,
            String name, String otp) {
        String subject = "🔐 Your SplitMate Password Reset Code";
        String content =
            "<p>Use this code to reset your password:</p>" +
            "<div style='background:#1a1612;color:#fff;" +
            "padding:24px;border-radius:12px;" +
            "text-align:center;margin:20px 0'>" +
            "<p style='font-size:36px;font-weight:bold;" +
            "letter-spacing:0.3em;color:#e8896d;margin:0'>" +
            otp + "</p></div>" +
            "<p style='color:#999;font-size:13px'>" +
            "This code expires in 10 minutes. " +
            "Do not share it with anyone.</p>";

        sendEmail(toEmail, name, subject,
            buildEmail(name, "Your password reset code",
                content, "Go to SplitMate", baseUrl));
    }

    private String buildEmail(String name, String title,
            String content, String buttonText,
            String buttonUrl) {
        return "<!DOCTYPE html><html>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='margin:0;padding:0;" +
            "font-family:Arial,sans-serif;background:#f9f9f9'>" +
            "<div style='max-width:560px;margin:40px auto;" +
            "background:#fff;border-radius:12px;overflow:hidden;" +
            "box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
            "<div style='background:#1a1612;padding:28px 32px'>" +
            "<h1 style='margin:0;color:#fff;font-size:24px'>" +
            "Split<span style='color:#e8896d;" +
            "font-style:italic'>Mate</span></h1>" +
            "<p style='margin:4px 0 0;color:#888;font-size:13px'>" +
            "Split expenses, not friendships</p></div>" +
            "<div style='padding:32px'>" +
            "<p style='color:#333;font-size:16px'>" +
            "Hey <strong>" + name + "</strong>! 👋</p>" +
            "<h2 style='color:#1a1612;font-size:20px;" +
            "margin:16px 0'>" + title + "</h2>" +
            content +
            "<div style='text-align:center;margin:28px 0'>" +
            "<a href='" + buttonUrl + "' " +
            "style='background:#c9623f;color:#fff;" +
            "padding:14px 32px;border-radius:8px;" +
            "text-decoration:none;font-weight:bold;" +
            "font-size:15px;display:inline-block'>" +
            buttonText + " →</a></div></div>" +
            "<div style='background:#f5f5f5;" +
            "padding:16px 32px;text-align:center'>" +
            "<p style='color:#999;font-size:12px;margin:0'>" +
            "SplitMate · Split expenses, not friendships 💸" +
            "</p></div></div></body></html>";
    }
}