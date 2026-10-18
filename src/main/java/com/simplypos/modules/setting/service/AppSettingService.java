package com.simplypos.modules.setting.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.setting.entity.AppSetting;
import com.simplypos.modules.setting.repository.AppSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppSettingService {

    private final AppSettingRepository repo;

    // ============ PATHS ============
    private static final String UPLOAD_DIR = "uploads/settings";
    private static final String PUBLIC_URL_PREFIX = "/uploads/settings/";

    // ============ CACHE ============
    @Cacheable(value = "appSettings", key = "'all'")
    public Map<String, String> getAllAsMap() {
        Map<String, String> result = new HashMap<>();
        for (AppSetting s : repo.findAll()) {
            result.put(s.getSettingKey(), s.getSettingValue());
        }
        return result;
    }

    @Cacheable(value = "appSettings", key = "#key")
    public String getValue(String key) {
        return repo.findBySettingKey(key)
                .map(AppSetting::getSettingValue)
                .orElse(null);
    }

    @Cacheable(value = "appSettings", key = "'value:' + #key + ':' + #defaultValue")
    public String getValue(String key, String defaultValue) {
        return repo.findBySettingKey(key)
                .map(AppSetting::getSettingValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return repo.findBySettingKey(key)
                .map(AppSetting::asBoolean)
                .orElse(defaultValue);
    }

    // ============ ADMIN OPERATIONS ============

    public List<AppSetting> findAll() {
        return repo.findAllByOrderBySettingGroupAscSortOrderAsc();
    }

    public List<AppSetting> findByGroup(String group) {
        return repo.findBySettingGroupOrderBySortOrderAsc(group);
    }

    public AppSetting findByKey(String key) {
        return repo.findBySettingKey(key)
                .orElseThrow(() -> new BusinessException("Setting tidak ditemukan: " + key));
    }

    @Transactional
    @CacheEvict(value = "appSettings", allEntries = true)
    public AppSetting update(String key, String value) {
        AppSetting s = findByKey(key);
        s.setSettingValue(value);
        s.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("system"));
        return repo.save(s);
    }

    @Transactional
    @CacheEvict(value = "appSettings", allEntries = true)
    public void updateBatch(Map<String, String> values) {
        String username = SecurityUtils.getCurrentUsername().orElse("system");
        for (Map.Entry<String, String> entry : values.entrySet()) {
            repo.findBySettingKey(entry.getKey()).ifPresent(s -> {
                s.setSettingValue(entry.getValue());
                s.setUpdatedBy(username);
                repo.save(s);
            });
        }
    }

    @Transactional
    @CacheEvict(value = "appSettings", allEntries = true)
    public AppSetting uploadFile(String key, MultipartFile file) {
        AppSetting s = findByKey(key);

        if (file == null || file.isEmpty()) {
            throw new BusinessException("File tidak boleh kosong");
        }

        // ============ VALIDASI UKURAN ============
        // Favicon: max 100KB, Gambar biasa: max 500KB
        boolean isFavicon = "app.favicon.path".equals(key);
        long maxSize = isFavicon ? 100 * 1024L : 500 * 1024L;

        if (file.getSize() > maxSize) {
            String limitStr = isFavicon ? "100KB" : "500KB";
            throw new BusinessException("Ukuran file maksimal " + limitStr);
        }

        // ============ VALIDASI MIME TYPE ============
        String contentType = file.getContentType();
        boolean isImage = contentType != null && contentType.startsWith("image/");
        boolean isIco = "image/x-icon".equals(contentType)
                || "image/vnd.microsoft.icon".equals(contentType);

        if (!isImage && !isIco) {
            throw new BusinessException("File harus berupa gambar (PNG/JPG/SVG/ICO)");
        }

        try {
            // Buat folder jika belum ada
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            // Khusus favicon, default ke .png jika tidak ada extension
            if (extension.isEmpty() && isFavicon) {
                extension = ".png";
            }

            String filename = key.replace(".", "_") + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + extension;

            // Hapus file lama jika ada (agar tidak menumpuk)
            String oldValue = s.getSettingValue();
            if (oldValue != null && oldValue.startsWith(PUBLIC_URL_PREFIX)) {
                try {
                    String oldFilename = oldValue.substring(PUBLIC_URL_PREFIX.length());
                    Files.deleteIfExists(Paths.get(UPLOAD_DIR, oldFilename));
                } catch (IOException e) {
                    log.warn("Failed to delete old file: {}", oldValue, e);
                }
            }

            // Save new file
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // Update setting value
            s.setSettingValue(PUBLIC_URL_PREFIX + filename);
            s.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("system"));
            return repo.save(s);

        } catch (IOException e) {
            log.error("Failed to upload file for setting {}", key, e);
            throw new BusinessException("Gagal mengunggah file: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "appSettings", allEntries = true)
    public void deleteFile(String key) {
        AppSetting s = findByKey(key);
        String oldPath = s.getSettingValue();

        // Hapus file fisik jika ada
        if (oldPath != null && oldPath.startsWith(PUBLIC_URL_PREFIX)) {
            try {
                String filename = oldPath.substring(PUBLIC_URL_PREFIX.length());
                Files.deleteIfExists(Paths.get(UPLOAD_DIR, filename));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", oldPath, e);
            }
        }

        s.setSettingValue(null);
        s.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("system"));
        repo.save(s);
    }

    // ============ HELPER GETTERS ============

    public String getLoginHeadTitle() {
        return getValue("login.head.title", "Login – Simply PoS");
    }

    public String getLoginLogoPath() {
        return getValue("login.logo.path");
    }

    public String getLoginLogoText() {
        return getValue("login.logo.text", "PG");
    }

    public String getLoginTitle() {
        return getValue("login.title", "Simply PoS");
    }

    public String getLoginSubtitle() {
        return getValue("login.subtitle", "Business Management System");
    }

    public String getLoginFooter() {
        return getValue("login.footer", "© 2026 Simply PoS v1.0");
    }

    public String getLoginBackgroundColor() {
        return getValue("login.background.color", "#0078D4");
    }

    public String getSidebarLogoPath() {
        return getValue("sidebar.brand.logo.path");
    }

    public String getSidebarBrandText() {
        return getValue("sidebar.brand.text", "PG");
    }

    public String getSidebarBrandName() {
        return getValue("sidebar.brand.name", "Simply PoS");
    }

    public boolean isShowSidebarBrandName() {
        return getBoolean("sidebar.show.brand.name", true);
    }

    public String getAppName() {
        return getValue("app.name", "Simply PoS");
    }

    public String getAppFaviconPath() {
        return getValue("app.favicon.path");
    }
}