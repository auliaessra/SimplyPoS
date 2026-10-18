package com.simplypos.modules.purchase.controller;

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
import com.simplypos.modules.master.vendor.service.VendorService;
import com.simplypos.modules.purchase.dto.VendorPaymentRequest;
import com.simplypos.modules.purchase.service.VendorPaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/purchase/vendor-payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class VendorPaymentWebController {

    private final VendorPaymentService service;
    private final VendorService vendorService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("payments",
                service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Vendor Payment");
        return "purchase/payment/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        VendorPaymentRequest req = new VendorPaymentRequest();
        // Default: hari ini
        req.setPaymentDate(java.time.LocalDate.now());
        model.addAttribute("payment", req);
        model.addAttribute("vendors", vendorService.findByStore(null));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Buat Pembayaran Vendor");
        return "purchase/payment/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("payment") VendorPaymentRequest request,
            RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Pembayaran vendor berhasil dicatat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/vendor-payments";
    }
}