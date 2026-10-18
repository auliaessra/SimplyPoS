package com.simplypos.modules.finance.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.finance.service.FinancialReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/finance/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ReportsWebController {

    private final FinancialReportService reportService;
    private final StoreRepository storeRepository;

    @GetMapping("/trial-balance")
    public String trialBalance(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model) {
        LocalDate fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from)
                : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to)
                : LocalDate.now();

        model.addAttribute("rows", reportService.trialBalance(storeId, fromDate, toDate));
        populateCommon(model, storeId, fromDate, toDate, "Trial Balance");
        return "finance/reports/trial-balance";
    }

    @GetMapping("/profit-loss")
    public String profitLoss(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model) {
        LocalDate fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from)
                : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to)
                : LocalDate.now();

        model.addAttribute("report", reportService.profitLoss(storeId, fromDate, toDate));
        populateCommon(model, storeId, fromDate, toDate, "Profit & Loss");
        return "finance/reports/profit-loss";
    }

    @GetMapping("/balance-sheet")
    public String balanceSheet(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String asOf,
            Model model) {
        LocalDate asOfDate = (asOf != null && !asOf.isEmpty())
                ? LocalDate.parse(asOf)
                : LocalDate.now();

        model.addAttribute("report", reportService.balanceSheet(storeId, asOfDate));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("asOf", asOfDate);
        model.addAttribute("pageTitle", "Balance Sheet");
        return "finance/reports/balance-sheet";
    }

    private void populateCommon(Model model, Long storeId,
            LocalDate from, LocalDate to, String title) {
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("pageTitle", title);
    }
}