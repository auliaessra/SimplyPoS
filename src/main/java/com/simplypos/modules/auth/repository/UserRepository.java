package com.simplypos.modules.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByStoreIdOrderByUsernameAsc(Long storeId);

    List<User> findAllByOrderByUsernameAsc();

    /**
     * Search user dengan filter store + keyword.
     *
     * TRICK: Jangan pakai NULL parameter.
     * - storeId: gunakan -1L untuk "semua cabang" (tidak ada store dengan ID -1)
     * - q: gunakan string kosong "" untuk "semua user" (pattern %% match semua)
     *
     * Ini menghindari masalah PostgreSQL:
     * "cannot cast type bytea to bigint"
     * yang muncul ketika parameter NULL dikirim tanpa tipe eksplisit.
     */
    @Query("""
                SELECT u FROM User u
                WHERE (:storeId = -1L OR u.store.id = :storeId)
                  AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                       OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
                ORDER BY u.username ASC
            """)
    Page<User> search(@Param("storeId") Long storeId,
            @Param("q") String q,
            Pageable pageable);
}