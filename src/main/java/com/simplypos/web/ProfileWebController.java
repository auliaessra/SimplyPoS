package com.simplypos.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.master.user.dto.PasswordChangeRequest;
import com.simplypos.modules.master.user.dto.ProfileUpdateRequest;
import com.simplypos.modules.master.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileWebController {

    private final UserService service;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("profile", service.getCurrentUser());
        model.addAttribute("profileForm", new ProfileUpdateRequest());
        model.addAttribute("passwordForm", new PasswordChangeRequest());
        model.addAttribute("pageTitle", "Profil Saya");
        return "profile/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("profileForm") ProfileUpdateRequest req,
            RedirectAttributes ra) {
        try {
            service.updateProfile(req);
            ra.addFlashAttribute("success", "Profil berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("passwordForm") PasswordChangeRequest req,
            RedirectAttributes ra) {
        try {
            service.changePassword(req);
            ra.addFlashAttribute("success", "Password berhasil diubah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/profile";
    }
}