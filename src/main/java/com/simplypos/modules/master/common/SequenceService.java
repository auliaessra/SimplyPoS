package com.simplypos.modules.master.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class SequenceService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Generate nomor dokumen berurutan, contoh: PO0001, PI0001, VPAY0001
     */
    @Transactional
    public synchronized String nextNumber(String name) {
        // Ambil prefix dan current value
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT prefix, current_value FROM sequences WHERE name = :name FOR UPDATE")
                .setParameter("name", name)
                .getSingleResult();

        String prefix = (String) row[0];
        long next = ((Number) row[1]).longValue() + 1;

        em.createNativeQuery(
                "UPDATE sequences SET current_value = :v, updated_at = CURRENT_TIMESTAMP WHERE name = :name")
                .setParameter("v", next)
                .setParameter("name", name)
                .executeUpdate();

        return prefix + String.format("%04d", next);
    }
}