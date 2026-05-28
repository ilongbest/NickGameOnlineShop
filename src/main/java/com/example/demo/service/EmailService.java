package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private final ConcurrentHashMap<String, String> otpCache = new ConcurrentHashMap<>();

    public String sendOtpEmail(String toEmail) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpCache.put(toEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận đăng ký G-SHOP");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút. Vui lòng không cung cấp mã này cho bất kỳ ai.");

        mailSender.send(message);
        return otp;
    }
        public boolean verifyOtp(String email, String otp) {
            return otp.equals(otpCache.get(email));
    }

        public void clearOtp(String email) {
            otpCache.remove(email);
    }

        public void sendWelcomeEmail(String toEmail, String username) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Chào mừng bạn gia nhập G-SHOP!");
            message.setText("Chúc mừng " + username + ",\n\n" +
                    "Tài khoản của bạn đã được xác thực và kích hoạt thành công tại G-SHOP.\n" +
                    "Bây giờ bạn có thể đăng nhập và trải nghiệm các dịch vụ của chúng tôi.\n\n" +
                    "Trân trọng,\nĐội ngũ G-SHOP.");

            mailSender.send(message);
    }
}
