package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.entity.WalletTopupRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTopupRequestRepository;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/topups")
public class AdminTopupController {

    @Autowired
    private WalletTopupRequestRepository walletTopupRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public String showTopupManagement(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                     Model model) {
        List<WalletTopupRequest> pendingTopups = walletTopupRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<WalletTopupRequest> processedPage = walletTopupRequestRepository.findByStatusNotOrderByCreatedAtDesc("PENDING", pageRequest);
        
        model.addAttribute("pendingTopups", pendingTopups);
        model.addAttribute("processedTopups", processedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", processedPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "topup-management";
    }

    @PostMapping("/approve")
    public String approveTopup(@RequestParam("requestId") Long requestId) {
        WalletTopupRequest request = walletTopupRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay yeu cau nap tien"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            return "redirect:/admin/topups?error=already_processed";
        }

        User user = request.getUser();
        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        user.setBalance(currentBalance.add(request.getAmount()));
        userRepository.save(user);

        request.setStatus("APPROVED");
        request.setApprovedAt(LocalDateTime.now());
        walletTopupRequestRepository.save(request);

        // Send notification
        Notification notification = new Notification();
        notification.setUser(user);
        if ("CARD".equalsIgnoreCase(request.getRequestType())) {
            notification.setTitle("Nạp thẻ cào thành công");
            notification.setMessage(String.format("Bạn đã nạp thành công thẻ %s mệnh giá %s VNĐ. Số dư đã được cộng vào tài khoản.", 
                request.getCardType().toUpperCase(), 
                java.text.NumberFormat.getNumberInstance().format(request.getAmount())));
        } else {
            notification.setTitle("Nạp tiền thành công");
            notification.setMessage(String.format("Yêu cầu nạp %s VNĐ qua ngân hàng/ATM đã được duyệt. Số dư đã được cộng.", 
                java.text.NumberFormat.getNumberInstance().format(request.getAmount())));
        }
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return "redirect:/admin/topups?approved=true";
    }

    @PostMapping("/reject")
    public String rejectTopup(@RequestParam("requestId") Long requestId) {
        WalletTopupRequest request = walletTopupRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay yeu cau nap tien"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            return "redirect:/admin/topups?error=already_processed";
        }

        request.setStatus("REJECTED");
        request.setApprovedAt(LocalDateTime.now());
        walletTopupRequestRepository.save(request);

        // Send notification
        Notification notification = new Notification();
        notification.setUser(request.getUser());
        if ("CARD".equalsIgnoreCase(request.getRequestType())) {
            notification.setTitle("Nạp thẻ cào thất bại");
            notification.setMessage(String.format("Yêu cầu nạp thẻ %s mệnh giá %s VNĐ bị từ chối. Lý do: Thẻ sai hoặc đã được sử dụng.", 
                request.getCardType().toUpperCase(), 
                java.text.NumberFormat.getNumberInstance().format(request.getAmount())));
        } else {
            notification.setTitle("Nạp tiền thất bại");
            notification.setMessage(String.format("Yêu cầu nạp %s VNĐ qua ngân hàng/ATM bị từ chối.", 
                java.text.NumberFormat.getNumberInstance().format(request.getAmount())));
        }
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return "redirect:/admin/topups?rejected=true";
    }
}
