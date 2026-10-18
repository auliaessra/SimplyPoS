package com.simplypos.modules.master.store.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.master.store.dto.StoreRequest;
import com.simplypos.modules.master.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class StoreWebController {

    private final StoreService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stores", service.findAll());
        model.addAttribute("pageTitle", "Master Cabang");
        return "master/store/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("store", new StoreRequest());
        model.addAttribute("pageTitle", "Tambah Cabang");
        return "master/store/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("store") StoreRequest req, RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "Cabang berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/stores";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var s = service.findById(id);
        StoreRequest req = new StoreRequest();
        req.setCode(s.getCode());
        req.setName(s.getName());
        req.setAddress(s.getAddress());
        req.setPhone(s.getPhone());
        req.setEmail(s.getEmail());
        req.setManagerName(s.getManagerName());
        req.setIsActive(s.getIsActive());
        model.addAttribute("store", req);
        model.addAttribute("id", id);
        model.addAttribute("pageTitle", "Edit Cabang");
        return "master/store/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("store") StoreRequest req,
            RedirectAttributes ra) {
        try {
            service.update(id, req);
            ra.addFlashAttribute("success", "Cabang berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/stores";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggleActive(id);
            ra.addFlashAttribute("success", "Status cabang diubah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/stores";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Cabang berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/stores";
    }
}