package com.simplypos.modules.master.customer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.customer.dto.CustomerRequest;
import com.simplypos.modules.master.customer.dto.CustomerResponse;
import com.simplypos.modules.master.customer.entity.Customer;
import com.simplypos.modules.master.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repo;
    private final StoreRepository storeRepository;
    private final StoreContext storeContext;

    public List<CustomerResponse> findByStore(Long storeId) {
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

    public CustomerResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Customer tidak ditemukan")));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));
        if (repo.existsByStoreIdAndCode(storeId, request.getCode())) {
            throw new BusinessException("Kode customer sudah digunakan di toko ini");
        }
        Customer entity = Customer.builder()
                .store(store).code(request.getCode()).name(request.getName())
                .phone(request.getPhone()).email(request.getEmail())
                .address(request.getAddress())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        return toResponse(repo.save(entity));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Customer tidak ditemukan"));
        if (!entity.getCode().equals(request.getCode())
                && repo.existsByStoreIdAndCode(entity.getStore().getId(), request.getCode())) {
            throw new BusinessException("Kode customer sudah digunakan");
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
        Customer entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Customer tidak ditemukan"));
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

    private CustomerResponse toResponse(Customer e) {
        return new CustomerResponse(
                e.getId(), e.getStore().getId(), e.getStore().getName(),
                e.getCode(), e.getName(), e.getPhone(), e.getEmail(),
                e.getAddress(), e.getIsActive());
    }
}