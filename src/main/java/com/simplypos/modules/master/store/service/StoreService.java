package com.simplypos.modules.master.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.store.dto.StoreRequest;
import com.simplypos.modules.master.store.dto.StoreResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository repo;

    public List<StoreResponse> findAll() {
        return repo.findAllByOrderByCodeAsc().stream().map(this::toResponse).toList();
    }

    public List<StoreResponse> findActive() {
        return repo.findByIsActiveTrueOrderByCodeAsc().stream().map(this::toResponse).toList();
    }

    public StoreResponse findById(Long id) {
        return toResponse(repo.findById(id).orElseThrow(() -> new BusinessException("Cabang tidak ditemukan")));
    }

    @Transactional
    public StoreResponse create(StoreRequest request) {
        if (repo.existsByCode(request.getCode())) {
            throw new BusinessException("Kode cabang sudah digunakan");
        }
        Store store = Store.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .managerName(request.getManagerName())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        return toResponse(repo.save(store));
    }

    @Transactional
    public StoreResponse update(Long id, StoreRequest request) {
        Store store = repo.findById(id).orElseThrow(() -> new BusinessException("Cabang tidak ditemukan"));

        if (!store.getCode().equals(request.getCode())
                && repo.existsByCode(request.getCode())) {
            throw new BusinessException("Kode cabang sudah digunakan");
        }
        store.setCode(request.getCode());
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setManagerName(request.getManagerName());
        store.setIsActive(request.getIsActive());
        return toResponse(repo.save(store));
    }

    @Transactional
    public void delete(Long id) {
        Store store = repo.findById(id).orElseThrow(() -> new BusinessException("Cabang tidak ditemukan"));
        long userCount = repo.countUsersByStore(id);
        if (userCount > 0) {
            throw new BusinessException(
                    "Tidak dapat menghapus cabang ini karena masih memiliki " + userCount + " user");
        }
        repo.delete(store);
    }

    @Transactional
    public void toggleActive(Long id) {
        Store store = repo.findById(id).orElseThrow(() -> new BusinessException("Cabang tidak ditemukan"));
        store.setIsActive(!Boolean.TRUE.equals(store.getIsActive()));
        repo.save(store);
    }

    private StoreResponse toResponse(Store s) {
        return new StoreResponse(
                s.getId(), s.getCode(), s.getName(), s.getAddress(),
                s.getPhone(), s.getEmail(), s.getManagerName(),
                s.getIsActive(),
                repo.countUsersByStore(s.getId()),
                s.getCreatedAt());
    }
}