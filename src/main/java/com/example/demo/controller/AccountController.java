package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AccountController {
    @GetMapping("/AOV-nickgame")
    public String showAovPage() {
        return "AOV-nickgame";
    }

    @GetMapping("/nick-detail/{id}")
    public String showDetail(@PathVariable Long id) {
        return "nick-detail";
    }
    @GetMapping("/valorant-nickgame")
    public String showValorantPage() {
        return "valorant-nickgame";
    }

    @GetMapping("/freefire-nickgame")
    public String showFreeFirePage() {
        return "freefire-nickgame";
    }
    @GetMapping("/user-detail")
    public String showUserDetail() {
        return "user-detail";
    }

    @GetMapping("/mynick")
    public String showMyNickPage() {
        return "mynick";
    }

    @GetMapping("/cart-item")
    public String showCartPage() {
        return "Cart-item";
    }
}
