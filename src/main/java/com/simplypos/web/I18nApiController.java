package com.simplypos.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class I18nApiController {

    private final MessageSource messageSource;

    public I18nApiController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Ambil semua kunci pesan yang dibutuhkan JS.
     * Contoh: GET /api/i18n?keys=common.save,common.cancel
     */
    @GetMapping("/api/i18n")
    public Map<String, String> getMessages(
            @RequestParam(value = "keys", required = false) List<String> keys) {

        if (keys == null || keys.isEmpty()) {
            keys = Arrays.asList(
                    "common.save", "common.cancel", "common.delete", "common.confirm.delete",
                    "common.search", "common.reset", "common.no.data", "common.loading",
                    "error.stock.insufficient", "error.credit.needs.customer",
                    "error.amount.exceeds", "error.payment.insufficient",
                    "pos.cart.empty", "pos.walkin", "common.currency.prefix");
        }

        Map<String, String> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, messageSource.getMessage(key, null, key,
                    LocaleContextHolder.getLocale()));
        }
        return result;
    }
}