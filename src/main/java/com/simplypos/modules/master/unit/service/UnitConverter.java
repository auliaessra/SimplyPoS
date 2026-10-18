package com.simplypos.modules.master.unit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.unit.entity.Unit;
import com.simplypos.modules.master.unit.entity.UnitConversion;
import com.simplypos.modules.master.unit.repository.UnitConversionRepository;
import com.simplypos.modules.master.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service untuk mengkonversi qty antar unit.
 *
 * Alur pencarian faktor konversi:
 * 1. Cari konversi langsung: from → to
 * 2. Jika tidak ada, coba reverse: to → from (factor dibalik)
 * 3. Jika tidak ada, cari via base unit (rantai 2 hop):
 * from → base → to
 */
@Service
@RequiredArgsConstructor
public class UnitConverter {

    private final UnitConversionRepository conversionRepo;
    private final UnitRepository unitRepo;

    /**
     * Konversi qty dari unitFrom ke unitTo.
     */
    public BigDecimal convert(BigDecimal qty, Long fromUnitId, Long toUnitId) {
        if (qty == null)
            return BigDecimal.ZERO;
        if (fromUnitId == null || toUnitId == null) {
            throw new BusinessException("Unit tidak boleh null");
        }
        if (fromUnitId.equals(toUnitId)) {
            return qty;
        }

        BigDecimal factor = findFactor(fromUnitId, toUnitId);
        return qty.multiply(factor).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * Cari faktor konversi: 1 fromUnit = factor × toUnit.
     */
    public BigDecimal findFactor(Long fromId, Long toId) {
        if (fromId.equals(toId))
            return BigDecimal.ONE;

        // 1. Direct
        Optional<UnitConversion> direct = conversionRepo
                .findByFromUnitIdAndToUnitId(fromId, toId);
        if (direct.isPresent() && Boolean.TRUE.equals(direct.get().getIsActive())) {
            return direct.get().getFactor();
        }

        // 2. Reverse
        Optional<UnitConversion> reverse = conversionRepo
                .findByFromUnitIdAndToUnitId(toId, fromId);
        if (reverse.isPresent() && Boolean.TRUE.equals(reverse.get().getIsActive())) {
            return BigDecimal.ONE.divide(reverse.get().getFactor(), 8, RoundingMode.HALF_UP);
        }

        // 3. Via base unit
        Unit baseUnit = unitRepo.findByIsBaseTrue().orElse(null);
        if (baseUnit != null
                && !baseUnit.getId().equals(fromId)
                && !baseUnit.getId().equals(toId)) {

            BigDecimal fromToBase = findFactor(fromId, baseUnit.getId());
            BigDecimal baseToTo = findFactor(baseUnit.getId(), toId);
            if (fromToBase != null && baseToTo != null) {
                return fromToBase.multiply(baseToTo).setScale(8, RoundingMode.HALF_UP);
            }
        }

        throw new BusinessException(
                "Tidak ada konversi dari " + getUnitCode(fromId) + " ke " + getUnitCode(toId));
    }

    /**
     * Convert quantity in item's unit to base unit (ML).
     */
    public BigDecimal toBaseUnit(BigDecimal qty, Long itemUnitId) {
        Unit base = unitRepo.findByIsBaseTrue().orElse(null);
        if (base == null || itemUnitId == null || base.getId().equals(itemUnitId)) {
            return qty;
        }
        return convert(qty, itemUnitId, base.getId());
    }

    /**
     * Convert quantity from base unit (ML) to item's unit.
     */
    public BigDecimal fromBaseUnit(BigDecimal qty, Long itemUnitId) {
        Unit base = unitRepo.findByIsBaseTrue().orElse(null);
        if (base == null || itemUnitId == null || base.getId().equals(itemUnitId)) {
            return qty;
        }
        return convert(qty, base.getId(), itemUnitId);
    }

    public List<Unit> availableTargetUnits(Long fromUnitId) {
        return unitRepo.findByIsActiveTrueOrderBySortOrderAscCodeAsc();
    }

    private String getUnitCode(Long unitId) {
        return unitRepo.findById(unitId)
                .map(Unit::getCode)
                .orElse("?");
    }
}