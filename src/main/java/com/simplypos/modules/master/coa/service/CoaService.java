package com.simplypos.modules.master.coa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.modules.master.coa.dto.CoaRequest;
import com.simplypos.modules.master.coa.dto.CoaResponse;
import com.simplypos.modules.master.coa.entity.ChartOfAccount;
import com.simplypos.modules.master.coa.repository.ChartOfAccountRepository;
import com.simplypos.modules.master.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoaService {

    private final ChartOfAccountRepository repo;

    public List<CoaResponse> findAll() {
        return repo.findAllByOrderByAccountCode().stream().map(this::toResponse).toList();
    }

    public CoaResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Akun tidak ditemukan")));
    }

    @Transactional
    public CoaResponse create(CoaRequest request) {
        if (repo.existsByAccountCode(request.getAccountCode())) {
            throw new BusinessException("Kode akun sudah digunakan: " + request.getAccountCode());
        }
        ChartOfAccount parent = null;
        if (request.getParentId() != null) {
            parent = repo.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent akun tidak ditemukan"));
        }
        ChartOfAccount entity = ChartOfAccount.builder()
                .accountCode(request.getAccountCode())
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .parent(parent)
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        return toResponse(repo.save(entity));
    }

    @Transactional
    public CoaResponse update(Long id, CoaRequest request) {
        ChartOfAccount entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Akun tidak ditemukan"));
        if (!entity.getAccountCode().equals(request.getAccountCode())
                && repo.existsByAccountCode(request.getAccountCode())) {
            throw new BusinessException("Kode akun sudah digunakan");
        }
        entity.setAccountCode(request.getAccountCode());
        entity.setAccountName(request.getAccountName());
        entity.setAccountType(request.getAccountType());
        entity.setIsActive(request.getIsActive());

        if (request.getParentId() != null) {
            ChartOfAccount parent = repo.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent akun tidak ditemukan"));
            if (parent.getId().equals(id)) {
                throw new BusinessException("Akun tidak dapat menjadi parent dirinya sendiri");
            }
            entity.setParent(parent);
        } else {
            entity.setParent(null);
        }
        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ChartOfAccount entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Akun tidak ditemukan"));
        if (entity.getParent() != null) {
            // masih boleh dihapus, tapi lebih baik cek dulu
        }
        repo.delete(entity);
    }

    private CoaResponse toResponse(ChartOfAccount e) {
        return new CoaResponse(
                e.getId(),
                e.getAccountCode(),
                e.getAccountName(),
                e.getAccountType(),
                e.getParent() != null ? e.getParent().getId() : null,
                e.getParent() != null ? e.getParent().getAccountName() : null,
                e.getIsActive());
    }
}