package com.simplypos.modules.finance.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.finance.entity.GeneralLedger;

public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {

    List<GeneralLedger> findByStoreIdAndTransactionDateBetweenOrderByIdAsc(
            Long storeId, LocalDate from, LocalDate to);

    List<GeneralLedger> findByDocumentTypeAndDocumentNo(String documentType, String documentNo);

    @Query("SELECT COALESCE(SUM(gl.debit),0) FROM GeneralLedger gl " +
            "WHERE gl.store.id = :storeId AND gl.documentType = :docType AND gl.documentNo = :docNo")
    BigDecimal sumDebit(@Param("storeId") Long storeId,
            @Param("docType") String docType,
            @Param("docNo") String docNo);

    @Query("SELECT COALESCE(SUM(gl.credit),0) FROM GeneralLedger gl " +
            "WHERE gl.store.id = :storeId AND gl.documentType = :docType AND gl.documentNo = :docNo")
    BigDecimal sumCredit(@Param("storeId") Long storeId,
            @Param("docType") String docType,
            @Param("docNo") String docNo);

    @Query("SELECT gl FROM GeneralLedger gl WHERE " +
            "(:storeId IS NULL OR gl.store.id = :storeId) AND " +
            "(:coaId IS NULL OR gl.coa.id = :coaId) AND " +
            "gl.transactionDate BETWEEN :from AND :to " +
            "ORDER BY gl.transactionDate DESC, gl.id DESC")
    Page<GeneralLedger> search(@Param("storeId") Long storeId,
            @Param("coaId") Long coaId,
            @Param("from") java.time.LocalDate from,
            @Param("to") java.time.LocalDate to,
            Pageable pageable);

    // Trial Balance: sum debit/credit per CoA
    @Query("SELECT gl.coa.id, gl.coa.accountCode, gl.coa.accountName, " +
            "gl.coa.accountType, " +
            "COALESCE(SUM(gl.debit), 0), COALESCE(SUM(gl.credit), 0) " +
            "FROM GeneralLedger gl " +
            "WHERE (:storeId IS NULL OR gl.store.id = :storeId) " +
            "AND gl.transactionDate BETWEEN :from AND :to " +
            "GROUP BY gl.coa.id, gl.coa.accountCode, gl.coa.accountName, gl.coa.accountType " +
            "ORDER BY gl.coa.accountCode")
    List<Object[]> trialBalance(@Param("storeId") Long storeId,
            @Param("from") java.time.LocalDate from,
            @Param("to") java.time.LocalDate to);

    // Sum by CoA type in a period
    @Query("SELECT gl.coa.accountType, COALESCE(SUM(gl.debit),0), COALESCE(SUM(gl.credit),0) " +
            "FROM GeneralLedger gl " +
            "WHERE (:storeId IS NULL OR gl.store.id = :storeId) " +
            "AND gl.transactionDate BETWEEN :from AND :to " +
            "GROUP BY gl.coa.accountType")
    List<Object[]> sumByAccountType(@Param("storeId") Long storeId,
            @Param("from") java.time.LocalDate from,
            @Param("to") java.time.LocalDate to);
}