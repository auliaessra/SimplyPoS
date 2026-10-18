package com.simplypos.modules.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.inventory.dto.CustomerArSummaryResponse;
import com.simplypos.modules.inventory.dto.CustomerLedgerResponse;
import com.simplypos.modules.inventory.entity.CustomerLedger;
import com.simplypos.modules.inventory.repository.CustomerLedgerRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.customer.entity.Customer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerLedgerService {

    private final CustomerLedgerRepository repo;
    private final StoreContext storeContext;

    @Transactional
    public CustomerLedger post(Store store, Customer customer, String documentType,
            String documentNo, Long documentId,
            LocalDate transactionDate,
            BigDecimal amount, String description) {

        BigDecimal prevBalance = repo.sumByStoreAndCustomer(store.getId(), customer.getId());
        BigDecimal newBalance = prevBalance.add(amount);

        CustomerLedger ledger = CustomerLedger.builder()
                .store(store)
                .customer(customer)
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

    /**
     * Ambil semua ledger untuk customer tertentu.
     */
    public List<CustomerLedgerResponse> findByCustomer(Long customerId, Long storeId) {
        Long effectiveStore = resolveStoreId(storeId);
        return repo.findByStoreIdAndCustomerIdOrderByIdAsc(effectiveStore, customerId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Ringkasan AR per customer (hanya yang punya saldo > 0).
     */
    public List<CustomerArSummaryResponse> getArSummary(Long storeId) {
        Long effectiveStore = resolveStoreId(storeId);
        return repo.findCustomerArSummary(effectiveStore).stream()
                .map(row -> new CustomerArSummaryResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue(),
                        (String) row[5],
                        (BigDecimal) row[6],
                        ((Number) row[7]).longValue()))
                .toList();
    }

    /**
     * Total AR untuk summary dashboard.
     */
    public BigDecimal getTotalAr(Long storeId) {
        Long effectiveStore = resolveStoreId(storeId);
        return repo.findCustomerArSummary(effectiveStore).stream()
                .map(row -> (BigDecimal) row[6])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long resolveStoreId(Long override) {
        // 1. Override eksplisit (dari form/query param)
        if (override != null)
            return override;

        // 2. StoreContext (diisi filter dari header atau user.store_id)
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;

        // 3. Jika masih kosong:
        // - SUPER_ADMIN → null (akses lintas cabang)
        // - Role lain → error (karena mereka harus terikat cabang)
        if (SecurityUtils.isSuperAdmin()) {
            return null;
        }
        throw new BusinessException("error.store.context");
    }

    private CustomerLedgerResponse toResponse(CustomerLedger e) {
        return new CustomerLedgerResponse(
                e.getId(),
                e.getStore().getId(),
                e.getStore().getName(),
                e.getCustomer().getId(),
                e.getCustomer().getCode(),
                e.getCustomer().getName(),
                e.getDocumentType(),
                e.getDocumentNo(),
                e.getDocumentId(),
                e.getTransactionDate(),
                e.getAmount(),
                e.getBalance(),
                e.getDescription());
    }
}