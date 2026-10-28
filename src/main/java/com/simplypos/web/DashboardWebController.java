package com.simplypos.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.reporting.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final DashboardService service;
    private final StoreRepository storeRepository;

    @GetMapping("")
    public String redirect() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/")
    public String redirect2() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/web")
    public String redirectWeb() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/web/")
    public String redirectWeb2() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/web/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(@RequestParam(required = false) Long storeId, Model model) {

        model.addAttribute("kpi", service.kpi(storeId));
        model.addAttribute("salesTrend30", service.salesTrend(storeId, 30));
        model.addAttribute("salesTrend12m", service.salesTrendMonthly(storeId, 12));
        model.addAttribute("topProducts30", service.topProducts(storeId, 30, 5));
        model.addAttribute("purchaseTrend30", service.purchaseTrend(storeId, 30));

        model.addAttribute("isSuperAdmin", SecurityUtils.isSuperAdmin());
        model.addAttribute("stores",
                SecurityUtils.isSuperAdmin() ? storeRepository.findAll() : null);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }
}