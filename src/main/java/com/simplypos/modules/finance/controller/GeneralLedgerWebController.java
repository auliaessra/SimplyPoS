package com.simplypos.modules.finance.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.finance.repository.GeneralLedgerRepository;
import com.simplypos.modules.master.coa.repository.ChartOfAccountRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/finance/general-ledger")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class GeneralLedgerWebController {

    private final GeneralLedgerRepository repo;
    private final ChartOfAccountRepository coaRepo;
    private final StoreRepository storeRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long coaId,
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

        model.addAttribute("ledgers",
                repo.search(storeId, coaId, fromDate, toDate, PageRequest.of(page, 30)));
        model.addAttribute("accounts", coaRepo.findAllByOrderByAccountCode());
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedCoaId", coaId);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("pageTitle", "General Ledger");
        return "finance/general-ledger/list";
    }
}