package com.simplypos.modules.master.coa.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.master.coa.dto.CoaRequest;
import com.simplypos.modules.master.coa.service.CoaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/coa")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class CoaWebController {

    private final CoaService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", service.findAll());
        model.addAttribute("pageTitle", "Chart of Accounts");
        return "master/coa/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("coa", new CoaRequest());
        model.addAttribute("parents", service.findAll());
        model.addAttribute("pageTitle", "Tambah Akun");
        return "master/coa/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("coa") CoaRequest request, RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Akun berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/coa";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var account = service.findById(id);
        CoaRequest request = new CoaRequest();
        request.setAccountCode(account.getAccountCode());
        request.setAccountName(account.getAccountName());
        request.setAccountType(account.getAccountType());
        request.setParentId(account.getParentId());
        request.setIsActive(account.getIsActive());
        model.addAttribute("coa", request);
        model.addAttribute("id", id);
        model.addAttribute("parents", service.findAll());
        model.addAttribute("pageTitle", "Edit Akun");
        return "master/coa/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("coa") CoaRequest request, RedirectAttributes ra) {
        try {
            service.update(id, request);
            ra.addFlashAttribute("success", "Akun berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/coa";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Akun berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/coa";
    }
}