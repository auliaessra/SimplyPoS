package com.simplypos.modules.master.unit.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.unit.dto.UnitConversionRequest;
import com.simplypos.modules.master.unit.dto.UnitConversionResponse;
import com.simplypos.modules.master.unit.entity.Unit;
import com.simplypos.modules.master.unit.entity.UnitConversion;
import com.simplypos.modules.master.unit.repository.UnitConversionRepository;
import com.simplypos.modules.master.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnitConversionService {

    private final UnitConversionRepository repo;
    private final UnitRepository unitRepo;

    public List<UnitConversionResponse> findAll() {
        return repo.findAllByOrderByFromUnitSortOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    public UnitConversionResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Konversi tidak ditemukan")));
    }

    @Transactional
    public UnitConversionResponse create(UnitConversionRequest req) {
        Unit from = unitRepo.findById(req.getFromUnitId())
                .orElseThrow(() -> new BusinessException("Unit asal tidak ditemukan"));
        Unit to = unitRepo.findById(req.getToUnitId())
                .orElseThrow(() -> new BusinessException("Unit tujuan tidak ditemukan"));

        if (from.getId().equals(to.getId())) {
            throw new BusinessException("Unit asal dan tujuan tidak boleh sama");
        }
        if (repo.existsByFromUnitIdAndToUnitId(from.getId(), to.getId())) {
            throw new BusinessException("Konversi sudah ada untuk pasangan unit ini");
        }
        if (req.getFactor() == null || req.getFactor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Faktor harus lebih besar dari 0");
        }

        UnitConversion entity = UnitConversion.builder()
                .fromUnit(from)
                .toUnit(to)
                .factor(req.getFactor())
                .isActive(req.getIsActive() == null ? true : req.getIsActive())
                .notes(req.getNotes())
                .build();

        return toResponse(repo.save(entity));
    }

    @Transactional
    public UnitConversionResponse update(Long id, UnitConversionRequest req) {
        UnitConversion entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Konversi tidak ditemukan"));

        Unit from = unitRepo.findById(req.getFromUnitId())
                .orElseThrow(() -> new BusinessException("Unit asal tidak ditemukan"));
        Unit to = unitRepo.findById(req.getToUnitId())
                .orElseThrow(() -> new BusinessException("Unit tujuan tidak ditemukan"));

        if (from.getId().equals(to.getId())) {
            throw new BusinessException("Unit asal dan tujuan tidak boleh sama");
        }

        // Cek duplikat selain dirinya sendiri
        repo.findByFromUnitIdAndToUnitId(from.getId(), to.getId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(
                                "Konversi sudah ada untuk pasangan unit ini");
                    }
                });

        if (req.getFactor() == null || req.getFactor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Faktor harus lebih besar dari 0");
        }

        entity.setFromUnit(from);
        entity.setToUnit(to);
        entity.setFactor(req.getFactor());
        entity.setIsActive(req.getIsActive() == null ? true : req.getIsActive());
        entity.setNotes(req.getNotes());

        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        UnitConversion entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Konversi tidak ditemukan"));
        repo.delete(entity);
    }

    @Transactional
    public void toggleActive(Long id) {
        UnitConversion entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Konversi tidak ditemukan"));
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        repo.save(entity);
    }

    private UnitConversionResponse toResponse(UnitConversion c) {
        String expression = "1 " + c.getFromUnit().getCode()
                + " = " + c.getFactor().stripTrailingZeros().toPlainString()
                + " " + c.getToUnit().getCode();

        return new UnitConversionResponse(
                c.getId(),
                c.getFromUnit().getId(), c.getFromUnit().getCode(), c.getFromUnit().getName(),
                c.getToUnit().getId(), c.getToUnit().getCode(), c.getToUnit().getName(),
                c.getFactor(),
                c.getIsActive(),
                c.getNotes(),
                expression);
    }
}