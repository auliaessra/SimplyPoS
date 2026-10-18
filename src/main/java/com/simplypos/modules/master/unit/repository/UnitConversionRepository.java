package com.simplypos.modules.master.unit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.master.unit.entity.UnitConversion;

public interface UnitConversionRepository extends JpaRepository<UnitConversion, Long> {
    List<UnitConversion> findAllByOrderByFromUnitSortOrderAsc();

    List<UnitConversion> findByIsActiveTrueOrderByFromUnitSortOrderAsc();

    Optional<UnitConversion> findByFromUnitIdAndToUnitId(Long fromId, Long toId);

    boolean existsByFromUnitIdAndToUnitId(Long fromId, Long toId);

    List<UnitConversion> findByFromUnitIdOrderByToUnitSortOrderAsc(Long fromId);

    void deleteByFromUnitIdOrToUnitId(Long fromId, Long toId);
}