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

import com.simplypos.modules.purchase.dto.PurchaseOrderRequest;
import com.simplypos.modules.purchase.dto.PurchaseOrderResponse;
import com.simplypos.modules.purchase.service.PurchaseOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/purchase/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderResponse>> list(
            @RequestParam(required = false) Long storeId, Pageable pageable) {
        return ResponseEntity.ok(service.search(storeId, pageable).map(service::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(service.toResponse(service.create(request)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PurchaseOrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.confirm(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.toResponse(service.cancel(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}