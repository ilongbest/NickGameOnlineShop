package com.example.demo.controller;

import com.example.demo.service.CaptchaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;

    @GetMapping(value = "/api/captcha/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getCaptchaImage(HttpSession session) throws IOException {
        String captchaText = captchaService.generateCaptcha(session);
        BufferedImage image = captchaService.generateCaptchaImage(captchaText);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
