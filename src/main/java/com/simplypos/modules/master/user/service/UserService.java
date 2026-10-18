package com.simplypos.modules.master.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.entities.User;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.auth.repository.UserRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.user.dto.PasswordChangeRequest;
import com.simplypos.modules.master.user.dto.ProfileUpdateRequest;
import com.simplypos.modules.master.user.dto.UserRequest;
import com.simplypos.modules.master.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder passwordEncoder;

    /** SUPER_ADMIN: null (semua cabang). ADMIN: hanya cabangnya */
    private Long effectiveStoreFilter(Long override) {
        if (SecurityUtils.isSuperAdmin())
            return override;
        return null; // akan di-resolve di controller via current user
    }

    public Page<UserResponse> search(Long storeId, String q, Pageable pageable) {
        // Non-superadmin hanya lihat user di cabangnya
        if (!SecurityUtils.isSuperAdmin()) {
            User current = currentUser().orElse(null);
            if (current != null && current.getStore() != null) {
                storeId = current.getStore().getId();
            }
        }

        // ============ TRICK: Konversi NULL → nilai default ============
        // storeId null → -1L (tidak akan match store manapun, jadi = tampil semua)
        // q null/blank → "" (pattern %% akan match semua)
        Long effectiveStoreId = (storeId != null) ? storeId : -1L;
        String effectiveQ = (q != null && !q.isBlank()) ? q : "";

        return repo.search(effectiveStoreId, effectiveQ, pageable)
                .map(this::toResponse);
    }

    public UserResponse findById(Long id) {
        return toResponse(repo.findById(id).orElseThrow(() -> new BusinessException("User tidak ditemukan")));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (repo.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username sudah digunakan");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("Password minimal 6 karakter");
        }

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepo.findById(request.getStoreId())
                    .orElseThrow(() -> new BusinessException("Cabang tidak ditemukan"));
        }
        // SUPER_ADMIN tidak terikat cabang
        if (!"SUPER_ADMIN".equals(request.getRole()) && store == null) {
            throw new BusinessException("Cabang wajib diisi untuk role selain SUPER_ADMIN");
        }
        if ("SUPER_ADMIN".equals(request.getRole())) {
            store = null;
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .store(store)
                .status(request.getStatus() == null ? "ACTIVE" : request.getStatus())
                .build();

        return toResponse(repo.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException("User tidak ditemukan"));

        if (!user.getUsername().equals(request.getUsername())
                && repo.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username sudah digunakan");
        }

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepo.findById(request.getStoreId())
                    .orElseThrow(() -> new BusinessException("Cabang tidak ditemukan"));
        }
        if (!"SUPER_ADMIN".equals(request.getRole()) && store == null) {
            throw new BusinessException("Cabang wajib diisi untuk role selain SUPER_ADMIN");
        }
        if ("SUPER_ADMIN".equals(request.getRole())) {
            store = null;
        }

        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStore(store);
        user.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());

        // Password opsional di update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new BusinessException("Password minimal 6 karakter");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(repo.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException("User tidak ditemukan"));
        String currentUsername = SecurityUtils.getCurrentUsername().orElse("");
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("Tidak dapat menghapus akun sendiri");
        }
        repo.delete(user);
    }

    @Transactional
    public void toggleActive(Long id) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException("User tidak ditemukan"));
        String currentUsername = SecurityUtils.getCurrentUsername().orElse("");
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("Tidak dapat menonaktifkan akun sendiri");
        }
        user.setStatus("ACTIVE".equals(user.getStatus()) ? "INACTIVE" : "ACTIVE");
        repo.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("Password minimal 6 karakter");
        }
        User user = repo.findById(id).orElseThrow(() -> new BusinessException("User tidak ditemukan"));
        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);
    }

    // ==================== PROFILE ====================

    public UserResponse getCurrentUser() {
        return toResponse(currentUser().orElseThrow(() -> new BusinessException("User tidak ditemukan")));
    }

    @Transactional
    public UserResponse updateProfile(ProfileUpdateRequest req) {
        User user = currentUser().orElseThrow(() -> new BusinessException("User tidak ditemukan"));
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        return toResponse(repo.save(user));
    }

    @Transactional
    public void changePassword(PasswordChangeRequest req) {
        User user = currentUser().orElseThrow(() -> new BusinessException("User tidak ditemukan"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Password lama tidak sesuai");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException("Konfirmasi password tidak cocok");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        repo.save(user);
    }

    private java.util.Optional<User> currentUser() {
        return SecurityUtils.getCurrentUsername()
                .flatMap(repo::findByUsername);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getFullName(),
                u.getEmail(), u.getPhone(), u.getRole(),
                u.getStore() != null ? u.getStore().getId() : null,
                u.getStore() != null ? u.getStore().getName() : null,
                u.getStatus(),
                u.getLastLoginAt(),
                u.getCreatedAt());
    }
}