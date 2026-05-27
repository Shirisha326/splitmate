package com.splitmate.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${MAIL_USERNAME}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    private void sendEmail(String toEmail, String subject, String htmlContent) {

        try {

            URL url = new URL("https://api.resend.com/emails");

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization",
                    "Bearer " + resendApiKey);

            conn.setRequestProperty("Content-Type",
                    "application/json");

            conn.setDoOutput(true);

            JSONObject json = new JSONObject();

            json.put("from", "SplitMate <" + fromEmail + ">");
            json.put("to", toEmail);
            json.put("subject", subject);
            json.put("html", htmlContent);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                log.info("Email sent successfully to {}", toEmail);
            } else {
                log.error("Failed to send email. Response code: {}",
                        responseCode);
            }

        } catch (Exception e) {

            log.error("Failed to send email to {}: {}",
                    toEmail,
                    e.getMessage());
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