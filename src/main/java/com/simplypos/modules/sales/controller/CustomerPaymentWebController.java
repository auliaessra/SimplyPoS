package com.simplypos.modules.sales.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.customer.service.CustomerService;
import com.simplypos.modules.sales.dto.CustomerPaymentRequest;
import com.simplypos.modules.sales.service.CustomerPaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/sales/customer-payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class CustomerPaymentWebController {

    private final CustomerPaymentService service;
    private final CustomerService customerService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("payments", service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Customer Payments");
        return "sales/payment/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long soId,
            Model model) {
        CustomerPaymentRequest req = new CustomerPaymentRequest();
        req.setPaymentDate(LocalDate.now());

        model.addAttribute("payment", req);
        model.addAttribute("customers", customerService.findByStore(null)); // ← tambah
        model.addAttribute("preSelectedCustomerId", customerId); // ← opsional
        model.addAttribute("preSelectedSoId", soId); // ← opsional
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Buat Penerimaan Piutang");
        return "sales/payment/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("payment") CustomerPaymentRequest req,
            RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "Penerimaan piutang berhasil dicatat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/sales/customer-payments";
    }
}