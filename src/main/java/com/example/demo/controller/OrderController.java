package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CaptchaService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin("*")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyPurchasedAccounts(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }

        User currentUser = userRepository.findByUsername(principal.getName())
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()))
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(404).body("Khong tim thay user dang nhap");
        }

        List<Map<String, Object>> result = orderRepository.findByUserIdOrderByPurchaseDateDesc(currentUser.getId())
                .stream()
                .map(order -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("orderId", order.getId());
                    item.put("purchaseDate", order.getPurchaseDate());
                    item.put("amount", order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO);

                    if (order.getAccount() != null) {
                        item.put("accountId", order.getAccount().getId());
                        item.put("accountUsername", order.getAccount().getAccUsername());
                        item.put("accountPassword", order.getAccount().getAccPassword());
                        item.put("categoryName", order.getAccount().getCategory() != null
                                ? order.getAccount().getCategory().getName()
                                : "Game");
                    } else {
                        item.put("accountId", null);
                        item.put("accountUsername", "");
                        item.put("accountPassword", "");
                        item.put("categoryName", "Game");
                    }
                    return item;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyAccount(@RequestParam(required = false) Long userId,
                                        @RequestParam Long accountId,
                                        @RequestParam String captcha,
                                        Principal principal,
                                        HttpSession session) {
        try {
            // Kiểm tra captcha
            if (!captchaService.validateCaptcha(captcha, session)) {
                return ResponseEntity.badRequest().body("Mã captcha không chính xác! Vui lòng thử lại.");
            }

            Long resolvedUserId = userId;
            if (principal != null) {
                User currentUser = userRepository.findByUsername(principal.getName())
                        .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()))
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user đăng nhập"));
                resolvedUserId = currentUser.getId();
            }
            if (resolvedUserId == null) {
                throw new RuntimeException("Thiếu thông tin người dùng");
            }
            Order order = orderService.buyAccount(resolvedUserId, accountId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Có lỗi hệ thống xảy ra: " + e.getMessage());
        }
    }
}
