package com.example.reactive.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String to;
    private String subject;
    private String content;
    private boolean isHtml;

    public static EmailMessage createUserWelcomeEmail(String email, String name) {
        return EmailMessage.builder()
                .to(email)
                .subject("Welcome to join us!")
                .content(buildWelcomeContent(name))
                .isHtml(true)
                .build();
    }

    public static EmailMessage createUserUpdateEmail(String email, String name) {
        return EmailMessage.builder()
                .to(email)
                .subject("Your information has been updated")
                .content(buildUpdateContent(name))
                .isHtml(true)
                .build();
    }

    public static EmailMessage createUserDeleteEmail(String email) {
        return EmailMessage.builder()
                .to(email)
                .subject("Account deleted")
                .content(buildDeleteContent())
                .isHtml(true)
                .build();
    }

    private static String buildWelcomeContent(String name) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; padding: 30px;">
                        <h2 style="color: #333;">Welcome, %s!</h2>
                        <p style="color: #666; line-height: 1.6;">
                            Thank you for registering our service. Your account has been successfully created.
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            We're glad you joined us!
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            This email is sent automatically by the system, please do not reply.
                        </p>
                    </div>
                </body>
                </html>
                """, name);
    }

    private static String buildUpdateContent(String name) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; padding: 30px;">
                        <h2 style="color: #333;">Hello, %s!</h2>
                        <p style="color: #666; line-height: 1.6;">
                            Your account information has been successfully updated.
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            If this was not you, please contact us immediately.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            This email is sent automatically by the system, please do not reply.
                        </p>
                    </div>
                </body>
                </html>
                """, name);
    }

    private static String buildDeleteContent() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; padding: 30px;">
                        <h2 style="color: #333;">Account deleted</h2>
                        <p style="color: #666; line-height: 1.6;">
                            Your account has been deleted.
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            Thank you for using our service!
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            This email is sent automatically by the system, please do not reply.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }
}
