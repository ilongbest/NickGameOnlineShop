package com.example.demo.controller;

import com.example.demo.entity.AccountGame;
import com.example.demo.entity.AccountImage;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.entity.WalletTopupRequest;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTopupRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {
    @Autowired private AccountRepository accountRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletTopupRequestRepository walletTopupRequestRepository;

    @GetMapping
    public String showDashboard(Model model) {
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("newAccount", new AccountGame());
        model.addAttribute("pendingTopups", walletTopupRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING"));
        return "account-dashboard";
    }

    @PostMapping("/add")
    public String addAccount(@ModelAttribute("newAccount") AccountGame account,
                             @RequestParam("categoryId") Integer categoryId,
                             @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        Category cat = categoryRepository.findById(categoryId).orElse(null);
        account.setCategory(cat);
        account.setStatus(0);
        account.setCreatedAt(LocalDateTime.now());

        AccountGame savedAccount = accountRepository.save(account);

        List<AccountImage> images = saveAccountImages(savedAccount, imageFiles);
        if (!images.isEmpty()) {
            savedAccount.setImages(images);
            accountRepository.save(savedAccount);
        }
        return "redirect:/admin/accounts?success";
    }

    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);
        return "redirect:/admin/accounts?deleted";
    }

    @PostMapping("/add-balance")
    public String addBalance(@RequestParam("userId") Long userId,
                             @RequestParam("amount") BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        BigDecimal currentBalance = (user.getBalance() != null) ? user.getBalance() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/admin/accounts?error=invalid_amount";
        }
        user.setBalance(currentBalance.add(amount));
        userRepository.save(user);

        return "redirect:/admin/accounts?success_balance";
    }

    @PostMapping("/topup/approve")
    public String approveTopup(@RequestParam("requestId") Long requestId) {
        WalletTopupRequest request = walletTopupRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay yeu cau nap tien"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            return "redirect:/admin/accounts?topup=already_processed";
        }

        User user = request.getUser();
        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        user.setBalance(currentBalance.add(request.getAmount()));
        userRepository.save(user);

        request.setStatus("APPROVED");
        request.setApprovedAt(LocalDateTime.now());
        walletTopupRequestRepository.save(request);
        return "redirect:/admin/accounts?topup=approved";
    }

    @GetMapping("/detail/{id}")
    public String showAccountDetail(@PathVariable Long id, Model model) {
        AccountGame account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại: " + id));
        model.addAttribute("account", account);
        model.addAttribute("categories", categoryRepository.findAll());
        return "account-detail";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("account") AccountGame account,
                                @RequestParam("categoryId") Integer categoryId) {
        AccountGame existing = accountRepository.findById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ID: " + account.getId()));
        existing.setAccUsername(account.getAccUsername());
        existing.setAccPassword(account.getAccPassword());
        existing.setPrice(account.getPrice());
        existing.setDescription(account.getDescription());
        existing.setStatus(account.getStatus());
        existing.setTag(account.getTag());

        Category cat = categoryRepository.findById(categoryId).orElse(null);
        existing.setCategory(cat);

        accountRepository.save(existing);
        return "redirect:/admin/accounts/detail/" + account.getId() + "?updated=true";
    }

    private List<AccountImage> saveAccountImages(AccountGame account, MultipartFile[] imageFiles) {
        List<AccountImage> images = new ArrayList<>();
        if (imageFiles == null || imageFiles.length == 0) {
            return images;
        }

        try {
            String uploadDir = "uploads/accounts/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : imageFiles) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                String fileName = account.getId() + "_" + System.currentTimeMillis() + "_" + originalName.replaceAll("\\s+", "_");
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                AccountImage accountImage = new AccountImage();
                accountImage.setUrl("/uploads/accounts/" + fileName);
                accountImage.setAccount(account);
                images.add(accountImage);
            }
        } catch (Exception e) {
            throw new RuntimeException("Khong the luu anh nick: " + e.getMessage(), e);
        }

        return images;
    }
}
