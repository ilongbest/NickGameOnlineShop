package com.example.demo.service;

import com.example.demo.entity.AccountGame;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Order buyAccount(Long userId, Long accountId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        AccountGame account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản game không tồn tại"));

        if (!Integer.valueOf(0).equals(account.getStatus())) {
            throw new RuntimeException("Tài khoản này đã được bán hoặc đang bị khóa");
        }
        if (user.getBalance().compareTo(account.getPrice()) < 0) {
            throw new RuntimeException("Số dư của bạn không đủ để thực hiện giao dịch");
        }

        user.setBalance(user.getBalance().subtract(account.getPrice()));
        userRepository.save(user);
        account.setStatus(1);
        accountRepository.save(account);

        Order order = new Order();
        order.setUser(user);
        order.setAccount(account);
        order.setAmount(account.getPrice());
        order.setPurchaseDate(LocalDateTime.now());
        order.setStatus("SUCCESS");

        return orderRepository.save(order);
    }
}
