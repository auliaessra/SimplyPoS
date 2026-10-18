package com.simplypos.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Store tempat user bernaung.
     * NULL untuk SUPER_ADMIN (akses lintas cabang).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 190)
    private String email;

    @Column(length = 30)
    private String phone;

    /**
     * Role user: SUPER_ADMIN, ADMIN, CASHIER, STAFF.
     */
    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "avatar_path", length = 255)
    private String avatarPath;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== HELPER ====================

    /**
     * Cek apakah user aktif.
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Cek apakah user adalah SUPER_ADMIN (tidak terikat cabang).
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }

    /**
     * Cek apakah user adalah ADMIN.
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Cek apakah user adalah CASHIER.
     */
    public boolean isCashier() {
        return "CASHIER".equals(role);
    }

    /**
     * Cek apakah user adalah STAFF.
     */
    public boolean isStaff() {
        return "STAFF".equals(role);
    }

    /**
     * Ambil storeId user (null untuk SUPER_ADMIN).
     */
    public Long getStoreId() {
        return store != null ? store.getId() : null;
    }

    /**
     * Ambil nama store user.
     */
    public String getStoreName() {
        return store != null ? store.getName() : null;
    }

    // ==================== EQUALS / HASHCODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User other = (User) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}