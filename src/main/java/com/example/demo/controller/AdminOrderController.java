package com.example.demo.controller;

import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public String showOrderManagement(Model model) {
        model.addAttribute("orders", orderRepository.findAllByOrderByPurchaseDateDesc());
        return "Order-management";
    }
}
