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

import com.simplypos.modules.sales.dto.SalesOrderRequest;
import com.simplypos.modules.sales.dto.SalesOrderResponse;
import com.simplypos.modules.sales.service.SalesOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/sales/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CASHIER')")
public class SalesOrderController {

    private final SalesOrderService service;

    @GetMapping
    public ResponseEntity<Page<SalesOrderResponse>> list(
            @RequestParam(required = false) Long storeId, Pageable pageable) {
        return ResponseEntity.ok(service.search(storeId, pageable).map(service::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderRequest req) {
        return ResponseEntity.ok(service.toResponse(service.create(req)));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<SalesOrderResponse> voidOrder(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.voidOrder(id)));
    }
}