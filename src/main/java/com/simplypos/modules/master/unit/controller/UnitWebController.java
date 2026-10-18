package com.simplypos.modules.master.unit.controller;

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

import com.simplypos.modules.master.unit.dto.UnitRequest;
import com.simplypos.modules.master.unit.service.UnitService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/units")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UnitWebController {

    private final UnitService service;

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("units", service.findAll(q));
        model.addAttribute("q", q);
        model.addAttribute("pageTitle", "Master Unit");
        return "master/unit/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("unit", new UnitRequest());
        model.addAttribute("pageTitle", "Tambah Unit");
        return "master/unit/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("unit") UnitRequest req, RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "Unit berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/units";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var u = service.findById(id);
        UnitRequest req = new UnitRequest();
        req.setCode(u.getCode());
        req.setName(u.getName());
        req.setCategory(u.getCategory());
        req.setIsBase(u.getIsBase());
        req.setIsActive(u.getIsActive());
        req.setDescription(u.getDescription());
        req.setSortOrder(u.getSortOrder());

        model.addAttribute("unit", req);
        model.addAttribute("id", id);
        model.addAttribute("pageTitle", "Edit Unit");
        return "master/unit/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("unit") UnitRequest req,
            RedirectAttributes ra) {
        try {
            service.update(id, req);
            ra.addFlashAttribute("success", "Unit berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/units";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggleActive(id);
            ra.addFlashAttribute("success", "Status unit diubah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/units";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Unit berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/units";
    }
}