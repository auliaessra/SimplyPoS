package com.simplypos.modules.purchase.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simplypos.modules.purchase.dto.PurchaseInvoiceRequest;
import com.simplypos.modules.purchase.dto.PurchaseInvoiceResponse;
import com.simplypos.modules.purchase.service.PurchaseInvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/purchase/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService service;

    @GetMapping
    public ResponseEntity<Page<PurchaseInvoiceResponse>> list(
            @RequestParam(required = false) Long storeId, Pageable pageable) {
        return ResponseEntity.ok(service.search(storeId, pageable).map(service::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseInvoiceResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PurchaseInvoiceResponse> create(@Valid @RequestBody PurchaseInvoiceRequest request) {
        return ResponseEntity.ok(service.toResponse(service.create(request)));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<PurchaseInvoiceResponse> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.post(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}