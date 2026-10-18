package com.simplypos.modules.master.storeprice.controller;

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
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.service.ItemService;
import com.simplypos.modules.master.storeprice.dto.StorePriceRequest;
import com.simplypos.modules.master.storeprice.service.StorePriceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/store-prices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class StorePriceWebController {

    private final StorePriceService service;
    private final ItemService itemService;
    private final StoreRepository storeRepository;

    // ============ LIST ============
    @GetMapping
    public String list(@RequestParam(required = false) Long storeId, Model model) {
        model.addAttribute("prices", service.findByStore(storeId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Harga & Stok per Toko");
        return "master/storeprice/list";
    }

    // ============ CREATE ============
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("price", new StorePriceRequest());
        model.addAttribute("items",
                itemService.search("", PageRequest.of(0, 1000)).getContent());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());

        // ✅ WAJIB: set currentPrice = null agar tidak error SpEL
        model.addAttribute("currentPrice", null);
        model.addAttribute("id", null);

        model.addAttribute("pageTitle", "Tambah Harga Item");
        return "master/storeprice/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("price") StorePriceRequest request,
            RedirectAttributes ra) {
        try {
            service.upsert(request);
            ra.addFlashAttribute("success", "Harga item berhasil disimpan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/store-prices";
    }

    // ============ EDIT ============
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model,
            RedirectAttributes ra) {
        try {
            var p = service.findById(id);

            StorePriceRequest req = new StorePriceRequest();
            req.setItemId(p.getItemId());
            req.setStoreId(p.getStoreId());
            req.setBuyPrice(p.getBuyPrice());
            req.setSellPrice(p.getSellPrice());
            req.setMinStockMl(p.getMinStockMl());

            model.addAttribute("price", req);
            model.addAttribute("id", id);
            model.addAttribute("currentPrice", p);

            model.addAttribute("items",
                    itemService.search("", PageRequest.of(0, 1000)).getContent());
            model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
            model.addAttribute("stores", storeRepository.findAll());
            model.addAttribute("pageTitle", "Edit Harga Item");

            return "master/storeprice/form";

        } catch (BusinessException e) {
            // Record tidak ditemukan → redirect ke list dengan error
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/master/store-prices";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("price") StorePriceRequest request,
            RedirectAttributes ra) {
        try {
            service.update(id, request);
            ra.addFlashAttribute("success", "Harga item berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/store-prices";
    }

    // ============ DELETE ============
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Harga item berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/store-prices";
    }
}