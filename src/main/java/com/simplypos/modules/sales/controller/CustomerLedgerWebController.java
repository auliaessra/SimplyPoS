package com.simplypos.modules.sales.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.inventory.service.CustomerLedgerService;
import com.simplypos.modules.master.customer.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/sales/customer-ledgers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class CustomerLedgerWebController {

    private final CustomerLedgerService service;
    private final CustomerService customerService;
    private final StoreRepository storeRepository;

    /**
     * Halaman utama: daftar customer dengan saldo piutang > 0.
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long storeId, Model model) {
        model.addAttribute("summaries", service.getArSummary(storeId));
        model.addAttribute("totalAr", service.getTotalAr(storeId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Customer Ledgers (AR)");
        return "sales/ledger/list";
    }

    /**
     * Detail ledger per customer: semua mutasi piutang dengan running balance.
     */
    @GetMapping("/{customerId}")
    public String detail(@PathVariable Long customerId,
            @RequestParam(required = false) Long storeId,
            Model model) {
        model.addAttribute("ledgers", service.findByCustomer(customerId, storeId));
        model.addAttribute("customer", customerService.findById(customerId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Detail Piutang Customer");
        return "sales/ledger/detail";
    }
}