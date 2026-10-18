package com.simplypos.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.simplypos.modules.setting.dto.SettingGroupDto;
import com.simplypos.modules.setting.entity.AppSetting;
import com.simplypos.modules.setting.service.AppSettingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/web/settings")
@RequiredArgsConstructor
public class SettingsWebController {

    private final AppSettingService settings;

    /**
     * Definisi urutan & metadata grup settings.
     * Urutan grup mengikuti urutan di list ini.
     */
    private static final List<String[]> GROUP_META = List.of(
            new String[] { "LOGIN", "settings.login.section", "bi-box-arrow-in-right" },
            new String[] { "SIDEBAR", "settings.sidebar.section", "bi-list" },
            new String[] { "GENERAL", "settings.general.section", "bi-gear" });

    // ============ HELPER: Build List of Groups ============
    private List<SettingGroupDto> buildGroups() {
        List<AppSetting> all = settings.findAll();

        // Group by setting_group
        Map<String, List<AppSetting>> byGroup = all.stream()
                .collect(Collectors.groupingBy(AppSetting::getSettingGroup));

        List<SettingGroupDto> result = new ArrayList<>();

        // Iterasi berdasarkan urutan yang didefinisikan di GROUP_META
        for (String[] meta : GROUP_META) {
            String code = meta[0];
            String labelKey = meta[1];
            String icon = meta[2];

            List<AppSetting> groupSettings = byGroup.get(code);
            if (groupSettings == null || groupSettings.isEmpty()) {
                continue; // skip grup kosong
            }

            // Sort by sortOrder
            groupSettings.sort((a, b) -> Integer.compare(
                    a.getSortOrder() != null ? a.getSortOrder() : 0,
                    b.getSortOrder() != null ? b.getSortOrder() : 0));

            result.add(new SettingGroupDto(code, labelKey, icon, groupSettings));
        }

        // Tambahkan grup yang tidak ada di GROUP_META (fallback)
        for (Map.Entry<String, List<AppSetting>> entry : byGroup.entrySet()) {
            boolean alreadyAdded = result.stream()
                    .anyMatch(g -> g.getGroupCode().equals(entry.getKey()));
            if (!alreadyAdded) {
                List<AppSetting> list = entry.getValue();
                list.sort((a, b) -> Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0));
                result.add(new SettingGroupDto(
                        entry.getKey(),
                        "settings.group." + entry.getKey().toLowerCase(),
                        "bi-folder",
                        list));
            }
        }

        return result;
    }

    // ============ READ-ONLY VIEW ============
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String index(Model model) {
        model.addAttribute("groups", buildGroups());
        model.addAttribute("canEdit", com.simplypos.core.security.SecurityUtils.isSuperAdmin());
        model.addAttribute("pageTitle", "Pengaturan");
        return "settings/index";
    }

    // ============ EDIT FORM (SUPER_ADMIN only) ============
    @GetMapping("/edit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String edit(Model model) {
        model.addAttribute("groups", buildGroups());
        model.addAttribute("pageTitle", "Pengaturan");
        return "settings/edit";
    }

    // ============ SAVE ============
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String save(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        try {
            // Filter hanya key yang valid (ada di DB)
            List<AppSetting> allSettings = settings.findAll();
            java.util.Set<String> validKeys = allSettings.stream()
                    .map(AppSetting::getSettingKey)
                    .collect(Collectors.toSet());

            Map<String, String> filtered = params.entrySet().stream()
                    .filter(e -> validKeys.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            settings.updateBatch(filtered);
            ra.addFlashAttribute("success", "Pengaturan berhasil disimpan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/settings/edit";
    }

    // ============ UPLOAD FILE ============
    @PostMapping("/upload")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String uploadFile(@RequestParam("key") String key,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes ra) {
        try {
            settings.uploadFile(key, file);
            ra.addFlashAttribute("success", "File berhasil diunggah");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/settings/edit";
    }

    // ============ DELETE FILE ============
    @PostMapping("/delete-file")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String deleteFile(@RequestParam("key") String key, RedirectAttributes ra) {
        try {
            settings.deleteFile(key);
            ra.addFlashAttribute("success", "File berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/settings/edit";
    }
}