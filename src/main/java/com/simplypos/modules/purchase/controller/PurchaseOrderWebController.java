package com.simplypos.modules.purchase.controller;

import org.springframework.data.domain.PageRequest;
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
import com.simplypos.modules.purchase.dto.PurchaseOrderRequest;
import com.simplypos.modules.purchase.service.PurchaseOrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/purchase/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
public class PurchaseOrderWebController {

    private final PurchaseOrderService service;
    private final VendorService vendorService;
    private final ItemService itemService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("orders",
                service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Purchase Order");
        return "purchase/order/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("order", new PurchaseOrderRequest());
        model.addAttribute("vendors", vendorService.findByStore(null));
        model.addAttribute("items", itemService.search("", PageRequest.of(0, 1000)).getContent());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Buat Purchase Order");
        return "purchase/order/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("order") PurchaseOrderRequest request,
            RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Purchase Order berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/orders";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("order", service.toResponse(service.findById(id)));
        model.addAttribute("pageTitle", "Detail Purchase Order");
        return "purchase/order/view";
    }

    @PostMapping("/confirm/{id}")
    public String confirm(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.confirm(id);
            ra.addFlashAttribute("success", "PO dikonfirmasi");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/orders";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cancel(id);
            ra.addFlashAttribute("success", "PO dibatalkan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/orders";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "PO dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/purchase/orders";
    }
}