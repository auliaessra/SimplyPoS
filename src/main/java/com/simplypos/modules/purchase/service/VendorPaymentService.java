package com.simplypos.modules.purchase.service;

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
import com.simplypos.modules.inventory.service.VendorLedgerService;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.vendor.entity.Vendor;
import com.simplypos.modules.master.vendor.repository.VendorRepository;
import com.simplypos.modules.purchase.dto.VendorPaymentRequest;
import com.simplypos.modules.purchase.dto.VendorPaymentResponse;
import com.simplypos.modules.purchase.entity.PurchaseInvoice;
import com.simplypos.modules.purchase.entity.VendorPayment;
import com.simplypos.modules.purchase.repository.PurchaseInvoiceRepository;
import com.simplypos.modules.purchase.repository.VendorPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorPaymentService {

    private final VendorPaymentRepository repo;
    private final PurchaseInvoiceRepository piRepo;
    private final VendorRepository vendorRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;
    private final VendorLedgerService vendorLedgerService;
    private final GeneralLedgerService glService;

    public Page<VendorPayment> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        if (storeId == null)
            storeId = storeContext.getCurrentStoreId();
        if (storeId == null)
            throw new BusinessException("Store context tidak tersedia");
        return repo.findByStoreId(storeId, pageable);
    }

    public VendorPayment findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Vendor Payment tidak ditemukan"));
    }

    /**
     * POSTING Vendor Payment:
     * - Kurangi AP via vendor_ledgers (amount negatif)
     * - Update paid_amount & status PI
     * - Auto-posting GL:
     * Debit : Hutang Usaha (2101)
     * Credit : Kas / Bank (1101/1102)
     */
    @Transactional
    public VendorPayment create(VendorPaymentRequest request) {
        // 1. Validasi PI wajib diisi (defensive, selain validasi annotation)
        if (request.getPurchaseInvoiceId() == null) {
            throw new BusinessException("Purchase Invoice wajib dipilih");
        }

        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));
        Vendor vendor = vendorRepo.findById(request.getVendorId())
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan"));

        if (!vendor.getStore().getId().equals(storeId)) {
            throw new BusinessException("Vendor tidak berada di cabang yang dipilih");
        }

        // 2. Ambil PI dan validasi
        PurchaseInvoice pi = piRepo.findById(request.getPurchaseInvoiceId())
                .orElseThrow(() -> new BusinessException("Purchase Invoice tidak ditemukan"));

        if (!pi.getVendor().getId().equals(vendor.getId())) {
            throw new BusinessException("Purchase Invoice bukan milik vendor tersebut");
        }

        if (!"POSTED".equals(pi.getStatus()) && !"PARTIAL_PAID".equals(pi.getStatus())) {
            throw new BusinessException("PI belum diposting atau sudah lunas (status: " + pi.getStatus() + ")");
        }

        BigDecimal outstanding = pi.getTotalAmount().subtract(pi.getPaidAmount());
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PI ini sudah lunas");
        }

        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessException(
                    "Jumlah pembayaran melebihi outstanding (sisa: Rp "
                            + outstanding.toPlainString() + ")");
        }

        // 3. Buat nomor payment
        String paymentNo = sequenceService.nextNumber("VPAY");

        VendorPayment vp = VendorPayment.builder()
                .store(store)
                .vendor(vendor)
                .purchaseInvoice(pi)
                .paymentNo(paymentNo)
                .paymentDate(request.getPaymentDate())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() == null ? "CASH" : request.getPaymentMethod())
                .bankAccount(request.getBankAccount())
                .notes(request.getNotes())
                .status("POSTED")
                .build();

        vp = repo.save(vp);

        // 4. Vendor Ledger – kurangi AP
        vendorLedgerService.post(
                store, vendor,
                "PAYMENT", paymentNo, vp.getId(),
                request.getPaymentDate(),
                request.getAmount().negate(),
                "Pembayaran untuk PI " + pi.getInvoiceNo());

        // 5. Update PI paid_amount & status
        BigDecimal newPaid = pi.getPaidAmount().add(request.getAmount());
        pi.setPaidAmount(newPaid);
        if (newPaid.compareTo(pi.getTotalAmount()) >= 0) {
            pi.setStatus("PAID");
        } else {
            pi.setStatus("PARTIAL_PAID");
        }
        piRepo.save(pi);

        // 6. General Ledger – Debit Hutang, Credit Kas/Bank
        String creditCoa = "TRANSFER".equalsIgnoreCase(request.getPaymentMethod())
                ? CoaCode.BANK
                : CoaCode.KAS;

        List<GeneralLedgerService.GlLine> debits = List.of(
                new GeneralLedgerService.GlLine(
                        CoaCode.HUTANG_USAHA, request.getAmount(),
                        "Pelunasan PI " + pi.getInvoiceNo()));
        List<GeneralLedgerService.GlLine> credits = List.of(
                new GeneralLedgerService.GlLine(
                        creditCoa, request.getAmount(),
                        "Pembayaran via " + request.getPaymentMethod()));

        glService.postDoubleEntry(
                store, "VENDOR_PAYMENT", paymentNo, vp.getId(),
                request.getPaymentDate(), debits, credits,
                "Pembayaran vendor " + paymentNo);

        return vp;
    }

    public VendorPaymentResponse toResponse(VendorPayment vp) {
        VendorPaymentResponse res = new VendorPaymentResponse();
        res.setId(vp.getId());
        res.setPaymentNo(vp.getPaymentNo());
        res.setStoreId(vp.getStore().getId());
        res.setStoreName(vp.getStore().getName());
        res.setVendorId(vp.getVendor().getId());
        res.setVendorName(vp.getVendor().getName());
        res.setPurchaseInvoiceId(vp.getPurchaseInvoice() != null ? vp.getPurchaseInvoice().getId() : null);
        res.setPurchaseInvoiceNo(vp.getPurchaseInvoice() != null ? vp.getPurchaseInvoice().getInvoiceNo() : null);
        res.setPaymentDate(vp.getPaymentDate());
        res.setAmount(vp.getAmount());
        res.setPaymentMethod(vp.getPaymentMethod());
        res.setBankAccount(vp.getBankAccount());
        res.setStatus(vp.getStatus());
        res.setNotes(vp.getNotes());
        res.setCreatedAt(vp.getCreatedAt());
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