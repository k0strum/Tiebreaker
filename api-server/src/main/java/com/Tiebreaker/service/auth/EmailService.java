package com.Tiebreaker.service.auth;

import com.Tiebreaker.constant.VerificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public void sendVerificationEmail(String toEmail, String token, VerificationType type, String nickname) {
        try {
            String subject = getEmailSubject(type);
            String content = createEmailContent(token, type, nickname);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // HTML 형식
            
            mailSender.send(message);
            log.info("인증 이메일 발송 완료: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
    
    private String getEmailSubject(VerificationType type) {
        switch (type) {
            case SIGNUP:
                return "[Tiebreaker] 이메일 인증을 완료해주세요";
            case PASSWORD_RESET:
                return "[Tiebreaker] 비밀번호 재설정";
            case EMAIL_CHANGE:
                return "[Tiebreaker] 이메일 변경 인증";
            default:
                return "[Tiebreaker] 인증 메일";
        }
    }
    
    private String createEmailContent(String token, VerificationType type, String nickname) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token + "&type=" + type.name();
        String userName = nickname != null ? nickname : "사용자";
        
        return createSignupVerificationEmail(verificationUrl, userName);
    }
    
    private String createSignupVerificationEmail(String verificationUrl, String nickname) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>이메일 인증</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #1e40af; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9fafb; }
                    .button { display: inline-block; background-color: #1e40af; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚾ Tiebreaker</h1>
                        <p>KBO 팬 플랫폼</p>
                    </div>
                    <div class="content">
                        <h2>안녕하세요, %s님!</h2>
                        <p>Tiebreaker 회원가입을 위한 이메일 인증을 완료해주세요.</p>
                        <p>아래 버튼을 클릭하여 이메일 인증을 완료하시면 서비스를 이용하실 수 있습니다.</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">이메일 인증하기</a>
                        </div>
                        <p>버튼이 작동하지 않는 경우, 아래 링크를 복사하여 브라우저에 붙여넣기 해주세요:</p>
                        <p style="word-break: break-all; color: #1e40af;">%s</p>
                        <p><strong>주의사항:</strong></p>
                        <ul>
                            <li>이 링크는 24시간 후에 만료됩니다.</li>
                            <li>본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>© 2024 Tiebreaker. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(nickname, verificationUrl, verificationUrl);
    }
    
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("이메일 발송 완료: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
} 