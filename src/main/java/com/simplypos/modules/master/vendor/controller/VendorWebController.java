package com.simplypos.modules.master.vendor.controller;

import java.util.List;

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
import com.simplypos.modules.master.vendor.dto.VendorRequest;
import com.simplypos.modules.master.vendor.dto.VendorResponse;
import com.simplypos.modules.master.vendor.service.VendorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','STAFF')")
public class VendorWebController {

    private final VendorService service;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId, Model model) {
        model.addAttribute("vendors", service.findByStore(storeId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Master Vendor");
        return "master/vendor/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("vendor", new VendorRequest());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Tambah Vendor");
        return "master/vendor/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("vendor") VendorRequest request, RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Vendor berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/vendors";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var v = service.findById(id);
        VendorRequest req = new VendorRequest();
        req.setCode(v.getCode());
        req.setName(v.getName());
        req.setPhone(v.getPhone());
        req.setEmail(v.getEmail());
        req.setAddress(v.getAddress());
        req.setIsActive(v.getIsActive());
        req.setStoreId(v.getStoreId());
        model.addAttribute("vendor", req);
        model.addAttribute("id", id);
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Edit Vendor");
        return "master/vendor/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("vendor") VendorRequest request,
            RedirectAttributes ra) {
        try {
            service.update(id, request);
            ra.addFlashAttribute("success", "Vendor berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/vendors";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Vendor berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/vendors";
    }

    @GetMapping("/by-store/{storeId}")
    public ResponseEntity<List<VendorResponse>> byStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(service.findByStore(storeId));
    }
}