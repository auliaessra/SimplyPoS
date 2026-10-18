package com.simplypos.modules.sales.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.finance.service.CoaCode;
import com.simplypos.modules.finance.service.GeneralLedgerService;
import com.simplypos.modules.inventory.repository.CustomerLedgerRepository;
import com.simplypos.modules.inventory.service.CustomerLedgerService;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.customer.entity.Customer;
import com.simplypos.modules.master.customer.repository.CustomerRepository;
import com.simplypos.modules.sales.dto.CustomerPaymentRequest;
import com.simplypos.modules.sales.dto.CustomerPaymentResponse;
import com.simplypos.modules.sales.entity.CustomerPayment;
import com.simplypos.modules.sales.entity.SalesOrder;
import com.simplypos.modules.sales.repository.CustomerPaymentRepository;
import com.simplypos.modules.sales.repository.SalesOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final CustomerPaymentRepository repo;
    private final SalesOrderRepository salesOrderRepo;
    private final CustomerRepository customerRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;
    private final CustomerLedgerService customerLedgerService;
    private final CustomerLedgerRepository customerLedgerRepository;
    private final GeneralLedgerService glService;

    public Page<CustomerPayment> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        if (storeId == null)
            storeId = storeContext.getCurrentStoreId();
        if (storeId == null)
            throw new BusinessException("Store context tidak tersedia");
        return repo.findByStoreId(storeId, pageable);
    }

    public CustomerPayment findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException("Customer Payment tidak ditemukan"));
    }

    @Transactional
    public CustomerPayment create(CustomerPaymentRequest request) {
        if (request.getSalesOrderId() == null) {
            throw new BusinessException("Sales Order wajib dipilih");
        }

        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));

        SalesOrder so = salesOrderRepo.findById(request.getSalesOrderId())
                .orElseThrow(() -> new BusinessException("Sales Order tidak ditemukan"));

        if (!so.getStore().getId().equals(storeId)) {
            throw new BusinessException("SO bukan milik cabang yang dipilih");
        }
        if (!"CREDIT".equals(so.getPaymentMethod())) {
            throw new BusinessException("SO bukan transaksi kredit");
        }
        if ("VOID".equals(so.getStatus())) {
            throw new BusinessException("SO sudah di-void");
        }

        Customer customer = so.getCustomer();
        if (customer == null) {
            throw new BusinessException("SO tidak memiliki customer");
        }

        // Hitung outstanding AR untuk SO ini
        BigDecimal outstanding = customerLedgerRepository
                .sumByStoreAndCustomer(storeId, customer.getId());

        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Customer ini tidak memiliki hutang");
        }

        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessException("Jumlah pembayaran melebihi piutang (Rp " + outstanding + ")");
        }

        String paymentNo = sequenceService.nextNumber("CPAY");

        CustomerPayment cp = CustomerPayment.builder()
                .store(store)
                .customer(customer)
                .salesOrder(so)
                .paymentNo(paymentNo)
                .paymentDate(request.getPaymentDate())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() == null ? "CASH" : request.getPaymentMethod())
                .bankAccount(request.getBankAccount())
                .notes(request.getNotes())
                .status("POSTED")
                .build();

        cp = repo.save(cp);

        // 1. Customer Ledger: kurangi AR
        customerLedgerService.post(
                store, customer,
                "RECEIPT", paymentNo, cp.getId(),
                request.getPaymentDate(),
                request.getAmount().negate(),
                "Penerimaan piutang " + paymentNo);

        // 2. General Ledger: Debit Kas/Bank, Credit Piutang
        String debitCoa = "TRANSFER".equalsIgnoreCase(request.getPaymentMethod())
                ? CoaCode.BANK
                : CoaCode.KAS;

        List<GeneralLedgerService.GlLine> debits = List.of(
                new GeneralLedgerService.GlLine(debitCoa, request.getAmount(),
                        "Penerimaan piutang dari " + customer.getName()));
        List<GeneralLedgerService.GlLine> credits = List.of(
                new GeneralLedgerService.GlLine(CoaCode.PIUTANG_USAHA, request.getAmount(),
                        "Pelunasan piutang " + paymentNo));

        glService.postDoubleEntry(
                store, "CUSTOMER_PAYMENT", paymentNo, cp.getId(),
                request.getPaymentDate(), debits, credits,
                "Penerimaan piutang " + paymentNo);

        return cp;
    }

    public CustomerPaymentResponse toResponse(CustomerPayment cp) {
        CustomerPaymentResponse res = new CustomerPaymentResponse();
        res.setId(cp.getId());
        res.setPaymentNo(cp.getPaymentNo());
        res.setStoreId(cp.getStore().getId());
        res.setStoreName(cp.getStore().getName());
        res.setCustomerId(cp.getCustomer().getId());
        res.setCustomerName(cp.getCustomer().getName());
        if (cp.getSalesOrder() != null) {
            res.setSalesOrderId(cp.getSalesOrder().getId());
            res.setSalesOrderNo(cp.getSalesOrder().getOrderNo());
        }
        res.setPaymentDate(cp.getPaymentDate());
        res.setAmount(cp.getAmount());
        res.setPaymentMethod(cp.getPaymentMethod());
        res.setBankAccount(cp.getBankAccount());
        res.setStatus(cp.getStatus());
        res.setNotes(cp.getNotes());
        res.setCreatedAt(cp.getCreatedAt());
        return res;
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
}