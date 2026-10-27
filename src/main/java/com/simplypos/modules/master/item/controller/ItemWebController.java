package com.simplypos.modules.master.item.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.master.item.dto.ItemRequest;
import com.simplypos.modules.master.item.service.ItemService;
import com.simplypos.modules.master.unit.service.UnitService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/master/items")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CASHIER','STAFF')")
public class ItemWebController {

    private final ItemService service;
    private final UnitService unitService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<?> items = service.search(q, PageRequest.of(page, 10));
        model.addAttribute("items", items);
        model.addAttribute("q", q);
        model.addAttribute("pageTitle", "Master Item");
        return "master/item/list";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("item") ItemRequest request, RedirectAttributes ra) {
        try {
            service.create(request);
            ra.addFlashAttribute("success", "Item berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/items";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("item", new ItemRequest());
        model.addAttribute("units", unitService.findActive()); // ← tambah
        model.addAttribute("pageTitle", "Tambah Item");
        return "master/item/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var item = service.findById(id);
        ItemRequest request = new ItemRequest();
        request.setCode(item.getCode());
        request.setName(item.getName());
        request.setUnitId(item.getUnitId()); // ← tambah
        request.setDescription(item.getDescription());
        request.setIsActive(item.getIsActive());
        request.setBarcode(item.getBarcode());
        model.addAttribute("item", request);
        model.addAttribute("id", id);
        model.addAttribute("units", unitService.findActive()); // ← tambah
        model.addAttribute("pageTitle", "Edit Item");
        return "master/item/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("item") ItemRequest request, RedirectAttributes ra) {
        try {
            service.update(id, request);
            ra.addFlashAttribute("success", "Item berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/items";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Item berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/master/items";
    }

    @GetMapping("/by-barcode")
    @ResponseBody
    public ResponseEntity<?> findByBarcode(@RequestParam String barcode) {
        try {
            var item = service.findByBarcode(barcode);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}