package com.simplypos.modules.master.unit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.unit.dto.UnitRequest;
import com.simplypos.modules.master.unit.dto.UnitResponse;
import com.simplypos.modules.master.unit.entity.Unit;
import com.simplypos.modules.master.unit.repository.UnitConversionRepository;
import com.simplypos.modules.master.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository repo;
    private final UnitConversionRepository conversionRepo;

    public List<UnitResponse> findAll(String q) {
        List<Unit> list = (q == null || q.isBlank())
                ? repo.findAllByOrderBySortOrderAscCodeAsc()
                : repo.search(q);
        return list.stream().map(this::toResponse).toList();
    }

    public List<UnitResponse> findActive() {
        return repo.findByIsActiveTrueOrderBySortOrderAscCodeAsc()
                .stream().map(this::toResponse).toList();
    }

    public UnitResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan")));
    }

    public Unit getEntity(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan"));
    }

    public Unit getBaseUnit() {
        return repo.findByIsBaseTrue()
                .orElseThrow(() -> new BusinessException("Base unit tidak ditemukan"));
    }

    @Transactional
    public UnitResponse create(UnitRequest req) {
        String code = req.getCode().trim().toUpperCase();
        if (repo.existsByCode(code)) {
            throw new BusinessException("Kode unit sudah digunakan: " + code);
        }
        Unit unit = Unit.builder()
                .code(code)
                .name(req.getName())
                .category(req.getCategory() == null ? "VOLUME" : req.getCategory())
                .isBase(Boolean.TRUE.equals(req.getIsBase()))
                .isActive(req.getIsActive() == null ? true : req.getIsActive())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder())
                .build();
        return toResponse(repo.save(unit));
    }

    @Transactional
    public UnitResponse update(Long id, UnitRequest req) {
        Unit unit = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan"));

        String newCode = req.getCode().trim().toUpperCase();
        if (!unit.getCode().equals(newCode) && repo.existsByCode(newCode)) {
            throw new BusinessException("Kode unit sudah digunakan");
        }

        // Base unit tidak boleh diubah menjadi non-base jika tidak ada base lain
        if (unit.isBase() && !Boolean.TRUE.equals(req.getIsBase())) {
            throw new BusinessException("Base unit tidak dapat diubah");
        }

        unit.setCode(newCode);
        unit.setName(req.getName());
        unit.setCategory(req.getCategory() == null ? "VOLUME" : req.getCategory());
        unit.setIsActive(req.getIsActive() == null ? true : req.getIsActive());
        unit.setDescription(req.getDescription());
        unit.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        // isBase tidak diubah setelah dibuat

        return toResponse(repo.save(unit));
    }

    @Transactional
    public void toggleActive(Long id) {
        Unit unit = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan"));

        if (unit.isBase()) {
            throw new BusinessException("Base unit tidak dapat dinonaktifkan");
        }
        unit.setIsActive(!Boolean.TRUE.equals(unit.getIsActive()));
        repo.save(unit);
    }

    @Transactional
    public void delete(Long id) {
        Unit unit = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan"));

        if (unit.isBase()) {
            throw new BusinessException("Base unit tidak dapat dihapus");
        }

        // Cek apakah unit digunakan oleh item
        long usedByItems = countItemsUsingUnit(id);
        if (usedByItems > 0) {
            throw new BusinessException(
                    "Unit ini digunakan oleh " + usedByItems + " item. Tidak dapat dihapus.");
        }

        // Hapus konversi terkait
        conversionRepo.deleteByFromUnitIdOrToUnitId(id, id);
        repo.delete(unit);
    }

    private long countItemsUsingUnit(Long unitId) {
        // Bisa inject ItemRepository untuk query
        // Untuk sekarang, gunakan method tambahan di UnitRepository
        return 0L; // TODO: inject ItemRepository
    }

    private UnitResponse toResponse(Unit u) {
        return new UnitResponse(
                u.getId(), u.getCode(), u.getName(), u.getCategory(),
                u.getIsBase(), u.getIsActive(), u.getDescription(), u.getSortOrder());
    }
}