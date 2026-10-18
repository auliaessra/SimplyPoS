package com.simplypos.modules.purchase.controller;

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

import com.simplypos.modules.purchase.dto.VendorPaymentRequest;
import com.simplypos.modules.purchase.dto.VendorPaymentResponse;
import com.simplypos.modules.purchase.service.VendorPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/purchase/vendor-payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class VendorPaymentController {

    private final VendorPaymentService service;

    @GetMapping
    public ResponseEntity<Page<VendorPaymentResponse>> list(
            @RequestParam(required = false) Long storeId, Pageable pageable) {
        return ResponseEntity.ok(service.search(storeId, pageable).map(service::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorPaymentResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<VendorPaymentResponse> create(@Valid @RequestBody VendorPaymentRequest request) {
        return ResponseEntity.ok(service.toResponse(service.create(request)));
    }
}