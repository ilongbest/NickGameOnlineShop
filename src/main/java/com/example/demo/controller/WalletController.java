package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.WalletTopupRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTopupRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class WalletController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletTopupRequestRepository walletTopupRequestRepository;

    @GetMapping("/order-payment")
    public String showOrderPaymentPage(Model model, Principal principal) {
        User user = findCurrentUser(principal)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung dang nhap"));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentBalance", user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        model.addAttribute("currentUserId", user.getId());
        return "order-payment";
    }

    @PostMapping("/api/v1/wallet/topup")
    @ResponseBody
    public ResponseEntity<?> topupWallet(@RequestParam("amount") BigDecimal amount,
                                         @RequestParam("transferMemo") String transferMemo,
                                         Principal principal) {
        if (amount == null || amount.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return ResponseEntity.badRequest().body("So tien nap toi thieu la 1,000 VND.");
        }
        if (transferMemo == null || transferMemo.isBlank()) {
            return ResponseEntity.badRequest().body("Thieu noi dung chuyen khoan.");
        }

        User user = findCurrentUser(principal)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung dang nhap"));

        WalletTopupRequest request = new WalletTopupRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setTransferMemo(transferMemo);
        request.setStatus("PENDING");
        walletTopupRequestRepository.save(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Yeu cau nap tien da duoc gui. Cho admin xac nhan de cong so du.");
        result.put("requestId", request.getId());
        result.put("username", user.getUsername());
        return ResponseEntity.ok(result);
    }

    private java.util.Optional<User> findCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()));
    }
}
