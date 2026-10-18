package com.simplypos.modules.master.vendor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.vendor.dto.VendorRequest;
import com.simplypos.modules.master.vendor.dto.VendorResponse;
import com.simplypos.modules.master.vendor.entity.Vendor;
import com.simplypos.modules.master.vendor.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository repo;
    private final StoreRepository storeRepository;
    private final StoreContext storeContext;

    /**
     * - SUPER_ADMIN + storeId null → semua vendor lintas cabang.
     * - Selain itu → fallback ke StoreContext (wajib).
     */
    public List<VendorResponse> findByStore(Long storeId) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByStoreIdAscCodeAsc()
                    .stream().map(this::toResponse).toList();
        }

        if (storeId == null) {
            storeId = storeContext.getCurrentStoreId();
        }
        if (storeId == null) {
            throw new BusinessException("Store context tidak tersedia. Kirim header X-Store-Id atau pilih store.");
        }
        return repo.findByStoreId(storeId).stream().map(this::toResponse).toList();
    }

    public VendorResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan")));
    }

    @Transactional
    public VendorResponse create(VendorRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));

        if (repo.existsByStoreIdAndCode(storeId, request.getCode())) {
            throw new BusinessException("Kode vendor sudah digunakan di toko ini");
        }
        Vendor entity = Vendor.builder()
                .store(store)
                .code(request.getCode())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        return toResponse(repo.save(entity));
    }

    @Transactional
    public VendorResponse update(Long id, VendorRequest request) {
        Vendor entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan"));

        if (!entity.getCode().equals(request.getCode())
                && repo.existsByStoreIdAndCode(entity.getStore().getId(), request.getCode())) {
            throw new BusinessException("Kode vendor sudah digunakan di toko ini");
        }
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setAddress(request.getAddress());
        entity.setIsActive(request.getIsActive());
        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Vendor entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan"));
        repo.delete(entity);
    }

    private Long resolveStoreId(Long override) {
        // 1. Override eksplisit (dari form/query param)
        if (override != null)
            return override;

        // 2. StoreContext (diisi filter dari header atau user.store_id)
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;

        // 3. Jika masih kosong:
        // - SUPER_ADMIN → null (akses lintas cabang)
        // - Role lain → error (karena mereka harus terikat cabang)
        if (SecurityUtils.isSuperAdmin()) {
            return null;
        }
        throw new BusinessException("error.store.context");
    }

    private VendorResponse toResponse(Vendor e) {
        return new VendorResponse(
                e.getId(),
                e.getStore().getId(),
                e.getStore().getName(),
                e.getCode(),
                e.getName(),
                e.getPhone(),
                e.getEmail(),
                e.getAddress(),
                e.getIsActive());
    }
}