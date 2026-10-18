package com.simplypos.modules.reporting.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.reporting.service.ReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ReportWebController {

    private final ReportService service;
    private final StoreRepository storeRepository;

    // ============ SALES REPORT ============
    @GetMapping("/sales")
    public String salesReport(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model) {
        LocalDate fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from)
                : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to)
                : LocalDate.now();

        model.addAttribute("report", service.salesReport(storeId, fromDate, toDate));
        populateCommon(model, storeId, fromDate, toDate, "Sales Report");
        return "reporting/sales";
    }

    // ============ PURCHASE REPORT ============
    @GetMapping("/purchase")
    public String purchaseReport(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model) {
        LocalDate fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from)
                : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to)
                : LocalDate.now();

        model.addAttribute("report", service.purchaseReport(storeId, fromDate, toDate));
        populateCommon(model, storeId, fromDate, toDate, "Purchase Report");
        return "reporting/purchase";
    }

    // ============ INVENTORY REPORT ============
    @GetMapping("/inventory")
    public String inventoryReport(@RequestParam(required = false) Long storeId,
            Model model) {
        model.addAttribute("report", service.inventoryReport(storeId));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Inventory Report");
        return "reporting/inventory";
    }

    // ============ AGING AR ============
    @GetMapping("/aging-ar")
    public String agingAr(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String asOf,
            Model model) {
        LocalDate asOfDate = (asOf != null && !asOf.isEmpty())
                ? LocalDate.parse(asOf)
                : LocalDate.now();

        model.addAttribute("report", service.agingAr(storeId, asOfDate));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("asOf", asOfDate);
        model.addAttribute("pageTitle", "AR Aging Report");
        return "reporting/aging-ar";
    }

    // ============ AGING AP ============
    @GetMapping("/aging-ap")
    public String agingAp(@RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String asOf,
            Model model) {
        LocalDate asOfDate = (asOf != null && !asOf.isEmpty())
                ? LocalDate.parse(asOf)
                : LocalDate.now();

        model.addAttribute("report", service.agingAp(storeId, asOfDate));
        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("asOf", asOfDate);
        model.addAttribute("pageTitle", "AP Aging Report");
        return "reporting/aging-ap";
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