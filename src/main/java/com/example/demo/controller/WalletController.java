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

    @PostMapping("/api/v1/wallet/topup/card")
    @ResponseBody
    public ResponseEntity<?> topupWalletCard(@RequestParam("cardType") String cardType,
                                             @RequestParam("amount") BigDecimal amount,
                                             @RequestParam("cardCode") String cardCode,
                                             @RequestParam("cardSerial") String cardSerial,
                                             Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập.");
        }
        if (cardType == null || cardType.isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn loại thẻ.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Mệnh giá thẻ không hợp lệ.");
        }
        if (cardCode == null || cardCode.isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập mã thẻ.");
        }
        if (cardSerial == null || cardSerial.isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập serial thẻ.");
        }

        User user = findCurrentUser(principal)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng đăng nhập."));

        WalletTopupRequest request = new WalletTopupRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setTransferMemo("Nạp thẻ cào: " + cardType.toUpperCase());
        request.setRequestType("CARD");
        request.setCardType(cardType);
        request.setCardCode(cardCode);
        request.setCardSerial(cardSerial);
        request.setStatus("PENDING");
        walletTopupRequestRepository.save(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Gửi yêu cầu nạp thẻ thành công. Chờ admin duyệt.");
        result.put("requestId", request.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/wallet/top/topup")
    @ResponseBody
    public ResponseEntity<?> getTopToppedUpUsers() {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 5);
        java.util.List<Object[]> results = walletTopupRequestRepository.findTopToppedUpUsers(pageRequest);
        
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            User u = (User) row[0];
            BigDecimal total = (BigDecimal) row[1];
            
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("rank", i + 1);
            
            String maskedUsername = u.getUsername();
            if (maskedUsername.length() > 3) {
                maskedUsername = maskedUsername.substring(0, 3) + "***";
            } else {
                maskedUsername = maskedUsername + "***";
            }
            map.put("username", maskedUsername);
            map.put("totalAmount", total);
            map.put("avatarUrl", u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank() ? u.getAvatarUrl() : "/images/default-avatar.png");
            list.add(map);
        }
        return ResponseEntity.ok(list);
    }

    private java.util.Optional<User> findCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()));
    }
}
