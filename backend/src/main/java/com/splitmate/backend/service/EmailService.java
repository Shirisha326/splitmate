package com.splitmate.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${gmail.client.id}")
    private String clientId;

    @Value("${gmail.client.secret}")
    private String clientSecret;

    @Value("${gmail.refresh.token}")
    private String refreshToken;

    @Value("${gmail.sender.email}")
    private String senderEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    private Gmail getGmailService() throws Exception {

        UserCredentials credentials =
                UserCredentials.newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRefreshToken(refreshToken)
                        .build();

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("SplitMate")
                .build();
    }

    private void sendEmail(String toEmail,
                           String subject,
                           String htmlContent) {

        try {

            Gmail service = getGmailService();

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);

            email.setFrom(new InternetAddress(senderEmail));
            email.addRecipient(
                    jakarta.mail.Message.RecipientType.TO,
                    new InternetAddress(toEmail)
            );

            email.setSubject(subject);
            email.setContent(htmlContent, "text/html; charset=utf-8");

            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream();

            email.writeTo(buffer);

            String encodedEmail =
                    Base64.encodeBase64URLSafeString(
                            buffer.toByteArray()
                    );

            Message message = new Message();
            message.setRaw(encodedEmail);

            service.users()
                    .messages()
                    .send("me", message)
                    .execute();

            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception e) {

            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    @Async
    public void sendMemberAddedEmail(String toEmail,
                                     String memberName,
                                     String groupName,
                                     boolean isNewUser) {

        String content;

        if (isNewUser) {

            content = buildEmail(
                    memberName,
                    "You've been added to " + groupName,
                    "<p>Your login details:</p>" +
                            "<p>📧 Email: " + toEmail + "</p>" +
                            "<p>🔑 Password: Splitmate@123</p>",
                    "Login to SplitMate",
                    baseUrl
            );

        } else {

            content = buildEmail(
                    memberName,
                    "You've been added to " + groupName,
                    "<p>You have been added to a new group <b>" +
                            groupName + "</b>.</p>",
                    "Check your group",
                    baseUrl
            );
        }

        sendEmail(
                toEmail,
                "You've been added to " + groupName + " on SplitMate! 🎉",
                content
        );
    }

    @Async
    public void sendExpenseAddedEmail(String toEmail,
                                      String memberName,
                                      String groupName,
                                      String description,
                                      String amount,
                                      String paidBy,
                                      String yourShare) {

        String content = buildEmail(
                memberName,
                "New expense in " + groupName,
                "<p>💳 Description: " + description + "</p>" +
                        "<p>💰 Total: ₹" + amount + "</p>" +
                        "<p>👤 Paid by: " + paidBy + "</p>" +
                        "<p>📊 Your share: ₹" + yourShare + "</p>",
                "View Expense",
                baseUrl
        );

        sendEmail(
                toEmail,
                "New expense added in " + groupName + " 🧾",
                content
        );
    }

    @Async
    public void sendSettleUpEmail(String toEmail,
                                  String memberName,
                                  String fromName,
                                  String amount,
                                  String groupName) {

        String content = buildEmail(
                memberName,
                fromName + " settled up with you!",
                "<p>" + fromName + " has settled ₹" + amount +
                        " with you in <b>" + groupName + "</b>.</p>",
                "View Settlement",
                baseUrl
        );

        sendEmail(
                toEmail,
                fromName + " settled up with you! ✅",
                content
        );
    }

    @Async
    public void sendOtpEmail(String toEmail,
                             String name,
                             String otp) {

        String content = buildEmail(
                name,
                "Your password reset code",
                "<div style='background:#1a1612;color:#fff;padding:24px;" +
                        "border-radius:12px;text-align:center;margin:20px 0'>" +
                        "<p style='font-size:36px;font-weight:bold;" +
                        "letter-spacing:0.3em;color:#e8896d;margin:0'>" +
                        otp +
                        "</p></div>" +
                        "<p style='color:#999;font-size:13px'>" +
                        "Expires in 10 minutes.</p>",
                "Go to SplitMate",
                baseUrl
        );

        sendEmail(
                toEmail,
                "🔐 Your SplitMate Password Reset Code",
                content
        );
    }

    private String buildEmail(String name,
                              String title,
                              String contentHtml,
                              String buttonText,
                              String buttonUrl) {

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;" +
                "background:#f5f0eb;font-family:Arial,sans-serif'>" +
                "<div style='max-width:520px;margin:40px auto;background:#fff;" +
                "border-radius:16px;overflow:hidden'>" +

                "<div style='background:#1a1612;padding:28px 32px;" +
                "text-align:center'>" +
                "<h1 style='color:#e8896d;margin:0'>SplitMate 💸</h1>" +
                "</div>" +

                "<div style='padding:32px'>" +
                "<p>Hey <strong>" + name + "</strong>,</p>" +
                "<h2 style='color:#1a1612'>" + title + "</h2>" +
                contentHtml +
                "</div>" +

                "<div style='padding:0 32px 24px;text-align:center'>" +
                "<a href='" + buttonUrl + "' style='background:#e8896d;" +
                "color:#fff;padding:12px 32px;border-radius:8px;" +
                "text-decoration:none;font-weight:bold'>" +
                buttonText +
                "</a></div>" +

                "<div style='background:#f5f0eb;padding:16px;text-align:center'>" +
                "<p style='color:#999;font-size:12px'>" +
                "SplitMate Team | Automated message</p>" +
                "</div></div></body></html>";
    }
}