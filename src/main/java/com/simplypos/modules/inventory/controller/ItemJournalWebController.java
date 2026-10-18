package com.simplypos.modules.inventory.controller;

import java.time.LocalDate;

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
import com.simplypos.modules.inventory.dto.ItemJournalRequest;
import com.simplypos.modules.inventory.service.ItemJournalService;
import com.simplypos.modules.master.item.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/inventory/item-journals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ItemJournalWebController {

    private final ItemJournalService service;
    private final ItemService itemService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("journals",
                service.search(storeId, PageRequest.of(page, 10)));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Item Journal");
        return "inventory/item-journal/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        ItemJournalRequest req = new ItemJournalRequest();
        req.setJournalDate(LocalDate.now());
        req.setReason("OPNAME");
        model.addAttribute("journal", req);
        model.addAttribute("items",
                itemService.search("", PageRequest.of(0, 1000)).getContent());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("pageTitle", "Item Journal Baru");
        return "inventory/item-journal/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("journal") ItemJournalRequest req,
            RedirectAttributes ra) {
        try {
            service.create(req);
            ra.addFlashAttribute("success", "Item Journal berhasil dibuat");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/inventory/item-journals";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("journal", service.toResponse(service.findById(id)));
        model.addAttribute("pageTitle", "Detail Item Journal");
        return "inventory/item-journal/view";
    }

    @PostMapping("/post/{id}")
    public String post(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.post(id);
            ra.addFlashAttribute("success", "Item Journal berhasil diposting (stok & GL ter-update)");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/inventory/item-journals";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cancel(id);
            ra.addFlashAttribute("success", "Item Journal dibatalkan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/inventory/item-journals";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Item Journal dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/inventory/item-journals";
    }
}