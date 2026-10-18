package com.simplypos.modules.master.customer.controller;

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
import com.simplypos.modules.master.customer.dto.CustomerRequest;
import com.simplypos.modules.master.customer.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CASHIER','STAFF')")
public class CustomerWebController {

    private final CustomerService service;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId, Model model) {
        model.addAttribute("customers", service.findByStore(storeId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Master Customer");
        return "master/customer/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("customer", new CustomerRequest());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Tambah Customer");
        return "master/customer/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("customer") CustomerRequest request, RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Customer berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/customers";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var c = service.findById(id);
        CustomerRequest req = new CustomerRequest();
        req.setCode(c.getCode());
        req.setName(c.getName());
        req.setPhone(c.getPhone());
        req.setEmail(c.getEmail());
        req.setAddress(c.getAddress());
        req.setIsActive(c.getIsActive());
        req.setStoreId(c.getStoreId());
        model.addAttribute("customer", req);
        model.addAttribute("id", id);
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Edit Customer");
        return "master/customer/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("customer") CustomerRequest request,
            RedirectAttributes ra) {
        try {
            service.update(id, request);
            ra.addFlashAttribute("success", "Customer berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/customers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Customer berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/customers";
    }
}