package com.simplypos.modules.purchase.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.item.service.ItemService;
import com.simplypos.modules.master.vendor.service.VendorService;
import com.simplypos.modules.purchase.dto.PurchaseInvoiceRequest;
import com.simplypos.modules.purchase.dto.PurchaseInvoiceResponse;
import com.simplypos.modules.purchase.service.PurchaseInvoiceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/purchase/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
public class PurchaseInvoiceWebController {

    private final PurchaseInvoiceService service;
    private final VendorService vendorService;
    private final ItemService itemService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("invoices",
                service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Purchase Invoice");
        return "purchase/invoice/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("invoice", new PurchaseInvoiceRequest());
        model.addAttribute("vendors", vendorService.findByStore(null));
        model.addAttribute("items", itemService.search("", PageRequest.of(0, 1000)).getContent());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Buat Purchase Invoice");
        return "purchase/invoice/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("invoice") PurchaseInvoiceRequest request,
            RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Purchase Invoice berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/invoices";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", service.toResponse(service.findById(id)));
        model.addAttribute("pageTitle", "Detail Purchase Invoice");
        return "purchase/invoice/view";
    }

    @PostMapping("/post/{id}")
    public String post(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.post(id);
            ra.addFlashAttribute("success", "PI berhasil diposting (stok & GL ter-update)");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/invoices";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "PI dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/invoices";
    }

    /**
     * Cari PI yang outstanding untuk vendor tertentu.
     * Dipakai di form Vendor Payment untuk dropdown PI.
     *
     * GET /v1/purchase/invoices/open?vendorId=1&storeId=1
     */
    @GetMapping("/open")
    public ResponseEntity<List<PurchaseInvoiceResponse>> findOpen(
            @RequestParam Long vendorId,
            @RequestParam(required = false) Long storeId) {
        return ResponseEntity.ok(service.findOpenByVendor(vendorId, storeId));
    }
}