package com.simplypos.modules.inventory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.simplypos.entities.Store;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_journals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "document_no", nullable = false, length = 50)
    private String documentNo;

    @Column(name = "journal_date", nullable = false)
    private LocalDate journalDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String reason = "OPNAME";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @OneToMany(mappedBy = "itemJournal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemJournalItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(ItemJournalItem item) {
        items.add(item);
        item.setItemJournal(this);
    }
}