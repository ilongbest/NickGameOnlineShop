package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/api/send-otp")
    @ResponseBody
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        try {
            emailService.sendOtpEmail(email);
            return ResponseEntity.ok("Mã xác thực đã được gửi!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String otp,
                                  Model model) {
        if (!emailService.verifyOtp(email, otp)) {
            model.addAttribute("error", "Mã OTP không chính xác hoặc đã hết hạn!");
            return "register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên tài khoản đã tồn tại!");
            return "register";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email da duoc su dung!");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setRole("USER");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        emailService.clearOtp(username);

        try {
            emailService.sendWelcomeEmail(email, username);
        } catch (Exception e) {
            System.out.println("Không thể gửi mail chào mừng: " + e.getMessage());
        }
        emailService.clearOtp(email);

        return "redirect:/login?success";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password/verify")
    public String verifyForgotPassword(@RequestParam String email,
                                       @RequestParam String otp,
                                       HttpSession session,
                                       Model model) {
        if (emailService.verifyOtp(email, otp)) {
            session.setAttribute("resetEmail", email);
            session.setAttribute("otpVerified", true);
            return "redirect:/reset-password";
        }

        model.addAttribute("error", "Mã OTP không chính xác!");
        return "forgot-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String password,
                                       HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");
        Boolean isVerified = (Boolean) session.getAttribute("otpVerified");

        if (email != null && isVerified != null && isVerified) {
            userRepository.findFirstByEmailOrderByIdAsc(email).ifPresent(user -> {
                user.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu
                userRepository.save(user);
            });

            emailService.clearOtp(email);
            session.invalidate();
            return "redirect:/login?resetSuccess";
        }

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(HttpSession session) {
        if (session.getAttribute("otpVerified") == null) {
            return "redirect:/forgot-password";
        }
        return "reset-password";
    }

}
