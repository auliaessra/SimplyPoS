package com.simplypos.modules.inventory.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.inventory.repository.ItemLedgerRepository;
import com.simplypos.modules.master.item.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/inventory/item-ledgers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ItemLedgerWebController {

    private final ItemLedgerRepository repo;
    private final ItemService itemService;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        LocalDate fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from)
                : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to)
                : LocalDate.now();

        Pageable pageable = PageRequest.of(page, 20);
        model.addAttribute("ledgers",
                repo.search(storeId, itemId, fromDate, toDate, pageable));
        model.addAttribute("items",
                itemService.search("", PageRequest.of(0, 1000)).getContent());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("pageTitle", "Item Ledger");
        return "inventory/item-ledger/list";
    }
}