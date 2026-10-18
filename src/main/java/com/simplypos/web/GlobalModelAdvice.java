package com.simplypos.web;

import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.simplypos.modules.setting.service.AppSettingService;

/**
 * Menambahkan atribut global ke semua model,
 * sehingga template bisa akses settings tanpa controller harus inject.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AppSettingService settings;

    public GlobalModelAdvice(AppSettingService settings) {
        this.settings = settings;
    }

    @ModelAttribute("settings")
    public Map<String, String> settings() {
        return settings.getAllAsMap();
    }

    @ModelAttribute("appName")
    public String appName() {
        return settings.getAppName();
    }

    @ModelAttribute("loginHeadTitle")
    public String loginHeadTitle() {
        return settings.getLoginHeadTitle();
    }

    @ModelAttribute("loginLogoPath")
    public String loginLogoPath() {
        return settings.getLoginLogoPath();
    }

    @ModelAttribute("loginLogoText")
    public String loginLogoText() {
        return settings.getLoginLogoText();
    }

    @ModelAttribute("loginTitle")
    public String loginTitle() {
        return settings.getLoginTitle();
    }

    @ModelAttribute("loginSubtitle")
    public String loginSubtitle() {
        return settings.getLoginSubtitle();
    }

    @ModelAttribute("loginFooter")
    public String loginFooter() {
        return settings.getLoginFooter();
    }

    @ModelAttribute("loginBgColor")
    public String loginBgColor() {
        return settings.getLoginBackgroundColor();
    }

    @ModelAttribute("sidebarLogoPath")
    public String sidebarLogoPath() {
        return settings.getSidebarLogoPath();
    }

    @ModelAttribute("sidebarBrandText")
    public String sidebarBrandText() {
        return settings.getSidebarBrandText();
    }

    @ModelAttribute("sidebarBrandName")
    public String sidebarBrandName() {
        return settings.getSidebarBrandName();
    }

    @ModelAttribute("showSidebarBrandName")
    public boolean showSidebarBrandName() {
        return settings.isShowSidebarBrandName();
    }

    @ModelAttribute("faviconPath")
    public String faviconPath() {
        return settings.getAppFaviconPath();
    }
}