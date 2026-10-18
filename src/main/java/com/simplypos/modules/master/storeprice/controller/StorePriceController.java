package com.simplypos.modules.master.storeprice.controller;

import java.util.List;

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

import com.simplypos.modules.master.storeprice.dto.StorePriceRequest;
import com.simplypos.modules.master.storeprice.dto.StorePriceResponse;
import com.simplypos.modules.master.storeprice.service.StorePriceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/master/store-prices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class StorePriceController {

    private final StorePriceService service;

    @GetMapping
    public ResponseEntity<List<StorePriceResponse>> list(@RequestParam(required = false) Long storeId) {
        return ResponseEntity.ok(service.findByStore(storeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorePriceResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<StorePriceResponse> upsert(@Valid @RequestBody StorePriceRequest request) {
        return ResponseEntity.ok(service.upsert(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}