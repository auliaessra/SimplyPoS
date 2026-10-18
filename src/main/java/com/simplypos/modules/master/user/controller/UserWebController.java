package com.simplypos.modules.master.user.controller;

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
import com.simplypos.modules.master.store.service.StoreService;
import com.simplypos.modules.master.user.dto.UserRequest;
import com.simplypos.modules.master.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UserWebController {

    private final UserService service;
    private final StoreService storeService;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("users",
                service.search(storeId, q, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeService.findActive() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("q", q);
        model.addAttribute("pageTitle", "Manajemen User");
        return "master/user/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new UserRequest());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeService.findActive());
        model.addAttribute("pageTitle", "Tambah User");
        return "master/user/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("user") UserRequest req, RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "User berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var u = service.findById(id);
        UserRequest req = new UserRequest();
        req.setUsername(u.getUsername());
        req.setFullName(u.getFullName());
        req.setEmail(u.getEmail());
        req.setPhone(u.getPhone());
        req.setRole(u.getRole());
        req.setStoreId(u.getStoreId());
        req.setStatus(u.getStatus());
        model.addAttribute("user", req);
        model.addAttribute("id", id);
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeService.findActive());
        model.addAttribute("pageTitle", "Edit User");
        return "master/user/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("user") UserRequest req,
            RedirectAttributes ra) {
        try {
            service.update(id, req);
            ra.addFlashAttribute("success", "User berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/users";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggleActive(id);
            ra.addFlashAttribute("success", "Status user diubah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/users";
    }

    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes ra) {
        try {
            service.resetPassword(id, newPassword);
            ra.addFlashAttribute("success", "Password berhasil direset");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "User berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/users";
    }
}