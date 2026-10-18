package com.simplypos.modules.sales.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simplypos.modules.sales.dto.CustomerPaymentRequest;
import com.simplypos.modules.sales.dto.CustomerPaymentResponse;
import com.simplypos.modules.sales.service.CustomerPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/sales/customer-payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class CustomerPaymentController {

    private final CustomerPaymentService service;

    @GetMapping
    public ResponseEntity<Page<CustomerPaymentResponse>> list(
            @RequestParam(required = false) Long storeId, Pageable pageable) {
        return ResponseEntity.ok(service.search(storeId, pageable).map(service::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerPaymentResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<CustomerPaymentResponse> create(@Valid @RequestBody CustomerPaymentRequest req) {
        return ResponseEntity.ok(service.toResponse(service.create(req)));
    }
}