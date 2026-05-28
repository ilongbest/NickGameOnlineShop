package com.example.demo.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class CaptchaService {
    private static final String CAPTCHA_SESSION_KEY = "captcha";
    private static final int WIDTH = 150;
    private static final int HEIGHT = 50;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;
    private static final Random RANDOM = new Random();

    public String generateCaptcha(HttpSession session) {
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            captchaText.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaText.toString());
        return captchaText.toString();
    }

    public BufferedImage generateCaptchaImage(String captchaText) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Nền trắng
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Đường xáo trộn
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 8; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Văn bản captcha
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(new Color(50, 50, 50));
        
        int x = 10;
        for (char c : captchaText.toCharArray()) {
            g2d.drawString(String.valueOf(c), x, 35);
            x += 22;
        }

        // Điểm nhiễu
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 50; i++) {
            int nx = RANDOM.nextInt(WIDTH);
            int ny = RANDOM.nextInt(HEIGHT);
            g2d.fillOval(nx, ny, 2, 2);
        }

        g2d.dispose();
        return image;
    }

    public boolean validateCaptcha(String inputCaptcha, HttpSession session) {
        String storedCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        if (storedCaptcha == null || inputCaptcha == null) {
            return false;
        }
        boolean isValid = storedCaptcha.equalsIgnoreCase(inputCaptcha);
        if (isValid) {
            session.removeAttribute(CAPTCHA_SESSION_KEY);
        }
        return isValid;
    }
}
