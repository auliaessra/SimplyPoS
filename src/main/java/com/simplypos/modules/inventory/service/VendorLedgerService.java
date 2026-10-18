package com.simplypos.modules.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.entities.Store;
import com.simplypos.modules.inventory.entity.VendorLedger;
import com.simplypos.modules.inventory.repository.VendorLedgerRepository;
import com.simplypos.modules.master.vendor.entity.Vendor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorLedgerService {

    private final VendorLedgerRepository repo;

    @Transactional
    public VendorLedger post(Store store, Vendor vendor, String documentType,
            String documentNo, Long documentId,
            LocalDate transactionDate,
            BigDecimal amount, String description) {

        BigDecimal prevBalance = repo.sumByStoreAndVendor(store.getId(), vendor.getId());
        BigDecimal newBalance = prevBalance.add(amount);

        VendorLedger ledger = VendorLedger.builder()
                .store(store)
                .vendor(vendor)
                .documentType(documentType)
                .documentNo(documentNo)
                .documentId(documentId)
                .transactionDate(transactionDate)
                .amount(amount)
                .balance(newBalance)
                .description(description)
                .build();

        return repo.save(ledger);
    }
}