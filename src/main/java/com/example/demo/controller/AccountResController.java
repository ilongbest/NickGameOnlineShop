package com.example.demo.controller;

import com.example.demo.entity.AccountGame;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin("*") // Cho phép Frontend gọi API từ domain khác
public class AccountResController {
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/category/{categoryId}")
    public Page<AccountGame> getAccountsByCategory(@PathVariable Long categoryId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "9") int size,
                                                   @RequestParam(required = false) Long searchId,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String tag,
                                                   @RequestParam(defaultValue = "true") boolean available) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return accountRepository.findAll(buildAccountFilter(categoryId, searchId, tag, available), pageable);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AccountGame> getAccountById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    private Specification<AccountGame> buildAccountFilter(Long categoryId, Long searchId, String tag, boolean available) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));

            if (available) {
                predicates.add(criteriaBuilder.equal(root.get("status"), 0));
            }

            if (searchId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), searchId));
            }

            if (tag != null && !tag.isBlank() && !"all".equalsIgnoreCase(tag)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("tag")), tag.trim().toUpperCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        if ("priceAsc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("priceDesc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
