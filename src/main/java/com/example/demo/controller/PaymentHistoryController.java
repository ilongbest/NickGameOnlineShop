package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.entity.WalletTopupRequest;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTopupRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping
public class PaymentHistoryController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private WalletTopupRequestRepository walletTopupRequestRepository;

    @GetMapping("/payment-history")
    public String showPaymentHistoryPage() {
        return "payment-history";
    }

    @GetMapping("/api/v1/transactions/me")
    @ResponseBody
    public ResponseEntity<?> getMyTransactions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }

        User user = userRepository.findByUsername(principal.getName())
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()))
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Khong tim thay user");
        }

        List<Map<String, Object>> transactions = new ArrayList<>();

        List<Order> orders = orderRepository.findByUserIdOrderByPurchaseDateDesc(user.getId());
        for (Order order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "BUY_NICK");
            item.put("status", "SUCCESS");
            item.put("amount", order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO);
            item.put("createdAt", order.getPurchaseDate());
            item.put("note", "Mua nick #" + (order.getAccount() != null ? order.getAccount().getId() : "N/A"));
            item.put("refId", order.getId());
            transactions.add(item);
        }

        List<WalletTopupRequest> topups = walletTopupRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (WalletTopupRequest topup : topups) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "TOPUP");
            item.put("status", topup.getStatus());
            item.put("amount", topup.getAmount() != null ? topup.getAmount() : BigDecimal.ZERO);
            item.put("createdAt", topup.getCreatedAt());
            item.put("note", "Nap tien - " + topup.getTransferMemo());
            item.put("refId", topup.getId());
            transactions.add(item);
        }

        transactions.sort((a, b) -> {
            LocalDateTime ta = parseDateTime(a.get("createdAt"));
            LocalDateTime tb = parseDateTime(b.get("createdAt"));
            return tb.compareTo(ta);
        });

        return ResponseEntity.ok(transactions);
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        return LocalDateTime.MIN;
    }
}
