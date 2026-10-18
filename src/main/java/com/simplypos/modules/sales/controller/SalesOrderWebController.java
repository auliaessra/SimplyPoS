package com.simplypos.modules.sales.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.customer.service.CustomerService;
import com.simplypos.modules.master.storeprice.service.StorePriceService;
import com.simplypos.modules.sales.dto.SalesOrderRequest;
import com.simplypos.modules.sales.dto.SalesOrderResponse;
import com.simplypos.modules.sales.service.SalesOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CASHIER')")
public class SalesOrderWebController {

    private final SalesOrderService service;
    private final StorePriceService priceService;
    private final CustomerService customerService;
    private final StoreRepository storeRepository;

    @GetMapping("/orders")
    public String orderList(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("orders", service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("q", q);
        model.addAttribute("pageTitle", "Sales Orders");
        return "sales/order/list";
    }

    @GetMapping("/orders/view/{id}")
    public String orderView(@PathVariable Long id, Model model) {
        model.addAttribute("order", service.toResponse(service.findById(id)));
        model.addAttribute("pageTitle", "Detail Sales Order");
        return "sales/order/view";
    }

    @PostMapping("/orders/void/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public String voidOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.voidOrder(id);
            ra.addFlashAttribute("success", "Sales Order berhasil di-void");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/sales/orders";
    }

    @GetMapping("/pos")
    public String posPage(@RequestParam(required = false) Long storeId, Model model) {
        Long effectiveStore = storeId;
        if (effectiveStore == null && !SecurityUtils.isSuperAdmin()) {
            // Ambil dari StoreContext (via header)
            effectiveStore = 1L; // fallback — sesuaikan dengan StoreContext
        }

        model.addAttribute("products", priceService.findByStore(effectiveStore));
        model.addAttribute("customers", customerService.findByStore(effectiveStore));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", effectiveStore);
        model.addAttribute("pageTitle", "Point of Sale");
        return "sales/pos/form";
    }

    @PostMapping("/pos")
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderRequest req) {
        return ResponseEntity.ok(service.toResponse(service.create(req)));
    }

    /**
     * Cari SO credit outstanding untuk customer tertentu.
     * GET /api/v1/sales/orders/open?customerId=1&storeId=1
     */
    @GetMapping("/open")
    public ResponseEntity<List<SalesOrderResponse>> findOpen(
            @RequestParam Long customerId,
            @RequestParam(required = false) Long storeId) {
        return ResponseEntity.ok(service.findOpenByCustomer(customerId, storeId));
    }
}