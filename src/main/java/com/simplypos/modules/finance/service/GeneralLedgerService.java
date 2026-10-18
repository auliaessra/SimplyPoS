package com.simplypos.modules.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.entities.Store;
import com.simplypos.modules.finance.entity.GeneralLedger;
import com.simplypos.modules.finance.repository.GeneralLedgerRepository;
import com.simplypos.modules.master.coa.entity.ChartOfAccount;
import com.simplypos.modules.master.coa.repository.ChartOfAccountRepository;
import com.simplypos.modules.master.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

/**
 * Service untuk auto-posting ke General Ledger.
 * Wajib memenuhi rule: Total Debit = Total Credit.
 */
@Service
@RequiredArgsConstructor
public class GeneralLedgerService {

    private final GeneralLedgerRepository repo;
    private final ChartOfAccountRepository coaRepo;

    /**
     * Posting pasangan jurnal double-entry.
     * 
     * @param debitEntries  list [kode COA, amount, description]
     * @param creditEntries list [kode COA, amount, description]
     */
    @Transactional
    public void postDoubleEntry(Store store, String documentType, String documentNo,
            Long documentId, LocalDate date,
            List<GlLine> debitEntries,
            List<GlLine> creditEntries,
            String generalDescription) {

        BigDecimal totalDebit = debitEntries.stream()
                .map(GlLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = creditEntries.stream()
                .map(GlLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException(
                    "Jurnal tidak balance: Debit = " + totalDebit + ", Credit = " + totalCredit);
        }

        List<GeneralLedger> entries = new ArrayList<>();
        for (GlLine line : debitEntries) {
            entries.add(buildEntry(store, line, documentType, documentNo, documentId, date,
                    generalDescription, line.amount(), BigDecimal.ZERO));
        }
        for (GlLine line : creditEntries) {
            entries.add(buildEntry(store, line, documentType, documentNo, documentId, date,
                    generalDescription, BigDecimal.ZERO, line.amount()));
        }

        repo.saveAll(entries);
    }

    private GeneralLedger buildEntry(Store store, GlLine line, String docType, String docNo,
            Long docId, LocalDate date, String generalDesc,
            BigDecimal debit, BigDecimal credit) {
        ChartOfAccount coa = coaRepo.findByAccountCode(line.coaCode())
                .orElseThrow(() -> new BusinessException("CoA tidak ditemukan: " + line.coaCode()));

        return GeneralLedger.builder()
                .store(store)
                .coa(coa)
                .documentType(docType)
                .documentNo(docNo)
                .documentId(docId)
                .transactionDate(date)
                .debit(debit)
                .credit(credit)
                .description(line.description() != null ? line.description() : generalDesc)
                .build();
    }

    public record GlLine(String coaCode, BigDecimal amount, String description) {
    }
}