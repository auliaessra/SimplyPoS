package com.simplypos.modules.master.unit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.master.unit.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Unit> findByIsBaseTrue();

    List<Unit> findAllByOrderBySortOrderAscCodeAsc();

    List<Unit> findByIsActiveTrueOrderBySortOrderAscCodeAsc();

    List<Unit> findByCategoryOrderBySortOrderAscCodeAsc(String category);

    @Query("""
                SELECT u FROM Unit u
                WHERE (:q IS NULL OR :q = ''
                       OR LOWER(u.code) LIKE LOWER(CONCAT('%', :q, '%'))
                       OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')))
                ORDER BY u.sortOrder ASC, u.code ASC
            """)
    List<Unit> search(@Param("q") String q);
}