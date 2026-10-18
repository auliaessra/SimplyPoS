package com.simplypos.web;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LocaleController {

    private final LocaleResolver localeResolver;

    public LocaleController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    /**
     * Ganti bahasa: /locale/set/id atau /locale/set/en
     * Redirect balik ke halaman asal.
     */
    @GetMapping("/locale/set/{lang}")
    public String setLocale(@PathVariable String lang,
            HttpServletRequest request,
            HttpServletResponse response) {
        Locale locale = "en".equalsIgnoreCase(lang)
                ? Locale.ENGLISH
                : new Locale("id");
        localeResolver.setLocale(request, response, locale);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/web/dashboard");
    }
}