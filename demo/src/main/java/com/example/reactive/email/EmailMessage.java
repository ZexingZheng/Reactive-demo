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
                .subject("欢迎加入我们!")
                .content(buildWelcomeContent(name))
                .isHtml(true)
                .build();
    }

    public static EmailMessage createUserUpdateEmail(String email, String name) {
        return EmailMessage.builder()
                .to(email)
                .subject("您的信息已更新")
                .content(buildUpdateContent(name))
                .isHtml(true)
                .build();
    }

    public static EmailMessage createUserDeleteEmail(String email) {
        return EmailMessage.builder()
                .to(email)
                .subject("账号已删除")
                .content(buildDeleteContent())
                .isHtml(true)
                .build();
    }

    private static String buildWelcomeContent(String name) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; padding: 30px;">
                        <h2 style="color: #333;">欢迎, %s!</h2>
                        <p style="color: #666; line-height: 1.6;">
                            感谢您注册我们的服务。您的账号已经成功创建。
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            我们很高兴您能加入我们!
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            此邮件由系统自动发送，请勿回复。
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
                        <h2 style="color: #333;">您好, %s!</h2>
                        <p style="color: #666; line-height: 1.6;">
                            您的账号信息已经成功更新。
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            如果这不是您本人的操作，请立即联系我们。
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            此邮件由系统自动发送，请勿回复。
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
                        <h2 style="color: #333;">账号已删除</h2>
                        <p style="color: #666; line-height: 1.6;">
                            您的账号已经被删除。
                        </p>
                        <p style="color: #666; line-height: 1.6;">
                            感谢您曾经使用我们的服务!
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            此邮件由系统自动发送，请勿回复。
                        </p>
                    </div>
                </body>
                </html>
                """;
    }
}
