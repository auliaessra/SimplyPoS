package com.simplypos.modules.master.unit.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.master.unit.dto.UnitConversionRequest;
import com.simplypos.modules.master.unit.service.UnitConversionService;
import com.simplypos.modules.master.unit.service.UnitService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/unit-conversions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class UnitConversionWebController {

    private final UnitConversionService service;
    private final UnitService unitService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("conversions", service.findAll());
        model.addAttribute("pageTitle", "Konversi Unit");
        return "master/unit/conversion-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("conversion", new UnitConversionRequest());
        model.addAttribute("units", unitService.findActive());
        model.addAttribute("pageTitle", "Tambah Konversi Unit");
        return "master/unit/conversion-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("conversion") UnitConversionRequest req,
            RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "Konversi berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/unit-conversions";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var c = service.findById(id);
        UnitConversionRequest req = new UnitConversionRequest();
        req.setFromUnitId(c.getFromUnitId());
        req.setToUnitId(c.getToUnitId());
        req.setFactor(c.getFactor());
        req.setIsActive(c.getIsActive());
        req.setNotes(c.getNotes());

        model.addAttribute("conversion", req);
        model.addAttribute("id", id);
        model.addAttribute("units", unitService.findActive());
        model.addAttribute("pageTitle", "Edit Konversi Unit");
        return "master/unit/conversion-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("conversion") UnitConversionRequest req,
            RedirectAttributes ra) {
        try {
            service.update(id, req);
            ra.addFlashAttribute("success", "Konversi berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/unit-conversions";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggleActive(id);
            ra.addFlashAttribute("success", "Status konversi diubah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/unit-conversions";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Konversi berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/unit-conversions";
    }
}