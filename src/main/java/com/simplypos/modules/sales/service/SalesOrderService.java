package com.simplypos.modules.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
import com.simplypos.modules.inventory.service.CustomerLedgerService;
import com.simplypos.modules.inventory.service.InventoryLedgerService;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.customer.entity.Customer;
import com.simplypos.modules.master.customer.repository.CustomerRepository;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;
import com.simplypos.modules.master.storeprice.entity.ItemStorePrice;
import com.simplypos.modules.master.storeprice.repository.ItemStorePriceRepository;
import com.simplypos.modules.sales.dto.SalesOrderRequest;
import com.simplypos.modules.sales.dto.SalesOrderResponse;
import com.simplypos.modules.sales.entity.SalesOrder;
import com.simplypos.modules.sales.entity.SalesOrderItem;
import com.simplypos.modules.sales.repository.SalesOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository repo;
    private final CustomerRepository customerRepo;
    private final ItemRepository itemRepo;
    private final ItemStorePriceRepository priceRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;
    private final InventoryLedgerService inventoryLedgerService;
    private final CustomerLedgerService customerLedgerService;
    private final GeneralLedgerService glService;

    public Page<SalesOrder> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        if (storeId == null)
            storeId = storeContext.getCurrentStoreId();
        if (storeId == null)
            throw new BusinessException("Store context tidak tersedia");
        return repo.findByStoreId(storeId, pageable);
    }

    public SalesOrder findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException("Sales Order tidak ditemukan"));
    }

    /**
     * Membuat Sales Order (POS). Setelah dibuat:
     * - Stock berkurang (item_ledgers)
     * - HPP tercatat
     * - Jika CREDIT → AR bertambah di customer_ledgers
     * - Auto-posting GL:
     * Debit : Kas / Bank / Piutang
     * Credit : Pendapatan
     * Debit : HPP
     * Credit : Persediaan
     */
    @Transactional
    public SalesOrder create(SalesOrderRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepo.findById(request.getCustomerId())
                    .orElseThrow(() -> new BusinessException("Customer tidak ditemukan"));
            if (!customer.getStore().getId().equals(storeId)) {
                throw new BusinessException("Customer bukan milik cabang ini");
            }
        }

        // Validasi metode pembayaran
        String method = request.getPaymentMethod() == null ? "CASH" : request.getPaymentMethod().toUpperCase();
        if (!List.of("CASH", "QRIS", "CREDIT").contains(method)) {
            throw new BusinessException("Metode pembayaran tidak valid");
        }
        if ("CREDIT".equals(method) && customer == null) {
            throw new BusinessException("Pembayaran CREDIT wajib memilih customer");
        }

        String orderNo = sequenceService.nextNumber("SO");

        SalesOrder so = SalesOrder.builder()
                .store(store)
                .customer(customer)
                .orderNo(orderNo)
                .orderDate(request.getOrderDate() != null
                        ? request.getOrderDate().atStartOfDay()
                        : java.time.LocalDateTime.now())
                .paymentMethod(method)
                .status("COMPLETED")
                .notes(request.getNotes())
                .createdBy(SecurityUtils.getCurrentUsername().orElse("system"))
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalHpp = BigDecimal.ZERO;

        // Simpan dulu header agar dapat ID
        so = repo.save(so);

        for (var line : request.getItems()) {
            Item item = itemRepo.findById(line.getItemId())
                    .orElseThrow(() -> new BusinessException("Item tidak ditemukan: " + line.getItemId()));

            ItemStorePrice isp = priceRepo.findByItemIdAndStoreId(item.getId(), storeId)
                    .orElseThrow(() -> new BusinessException(
                            "Harga item " + item.getCode() + " belum di-set untuk cabang ini"));

            if (isp.getStockQtyMl().compareTo(line.getQtyMl()) < 0) {
                throw new BusinessException("Stok item " + item.getCode()
                        + " tidak cukup (tersedia: " + isp.getStockQtyMl() + " ML)");
            }

            BigDecimal unitPrice = isp.getSellPrice();
            BigDecimal discPct = line.getDiscountPercent() == null ? BigDecimal.ZERO : line.getDiscountPercent();
            BigDecimal gross = unitPrice.multiply(line.getQtyMl());
            BigDecimal discAmt = gross.multiply(discPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(discAmt);

            BigDecimal unitCost = isp.getBuyPrice();
            BigDecimal lineHpp = unitCost.multiply(line.getQtyMl());

            SalesOrderItem soi = SalesOrderItem.builder()
                    .item(item)
                    .qtyMl(line.getQtyMl())
                    .unitPrice(unitPrice)
                    .discountPercent(discPct)
                    .totalPrice(net)
                    .unitCost(unitCost)
                    .totalCost(lineHpp)
                    .build();
            so.addItem(soi);

            subtotal = subtotal.add(gross);
            totalDiscount = totalDiscount.add(discAmt);
            totalHpp = totalHpp.add(lineHpp);
        }

        BigDecimal taxAmount = request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount();
        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(taxAmount);

        // Hitung paid & change
        BigDecimal paid;
        BigDecimal change = BigDecimal.ZERO;
        if ("CREDIT".equals(method)) {
            paid = BigDecimal.ZERO;
        } else {
            paid = request.getPaidAmount() != null ? request.getPaidAmount() : grandTotal;
            if (paid.compareTo(grandTotal) < 0) {
                throw new BusinessException("Jumlah bayar kurang dari total");
            }
            change = paid.subtract(grandTotal);
        }

        so.setSubtotal(subtotal);
        so.setDiscountAmount(totalDiscount);
        so.setTaxAmount(taxAmount);
        so.setTotalAmount(grandTotal);
        so.setPaidAmount(paid);
        so.setChangeAmount(change);

        so = repo.save(so);

        // === POSTING LEDGER ===

        // 1. Item Ledger: kurangi stok + catat HPP
        LocalDate txDate = so.getOrderDate().toLocalDate();
        for (SalesOrderItem soi : so.getItems()) {
            inventoryLedgerService.postMovement(
                    store, soi.getItem(),
                    "SALE", orderNo, so.getId(),
                    txDate,
                    soi.getQtyMl().negate(), // negatif = stok keluar
                    soi.getUnitCost(),
                    "Penjualan POS " + orderNo);
        }

        // 2. Customer Ledger: AR bertambah jika CREDIT
        if ("CREDIT".equals(method) && customer != null) {
            customerLedgerService.post(
                    store, customer,
                    "SALE", orderNo, so.getId(),
                    txDate,
                    grandTotal,
                    "Penjualan kredit " + orderNo);
        }

        // 3. General Ledger
        List<GeneralLedgerService.GlLine> debits = new ArrayList<>();
        List<GeneralLedgerService.GlLine> credits = new ArrayList<>();

        // Debit: Kas / Bank / Piutang
        String debitCoa;
        if ("CREDIT".equals(method)) {
            debitCoa = CoaCode.PIUTANG_USAHA;
        } else if ("QRIS".equals(method)) {
            debitCoa = CoaCode.BANK;
        } else {
            debitCoa = CoaCode.KAS;
        }
        debits.add(new GeneralLedgerService.GlLine(debitCoa, grandTotal, "Penerimaan penjualan"));

        // Credit: Pendapatan
        credits.add(new GeneralLedgerService.GlLine(CoaCode.PENJUALAN, grandTotal, "Pendapatan penjualan"));

        // Debit: HPP
        if (totalHpp.compareTo(BigDecimal.ZERO) > 0) {
            debits.add(new GeneralLedgerService.GlLine(CoaCode.HPP, totalHpp, "Harga pokok penjualan"));
            // Credit: Persediaan
            credits.add(new GeneralLedgerService.GlLine(CoaCode.PERSEDIAAN, totalHpp, "Pengurangan persediaan"));
        }

        glService.postDoubleEntry(
                store, "SALES_ORDER", orderNo, so.getId(),
                txDate, debits, credits,
                "Posting penjualan " + orderNo);

        return so;
    }

    @Transactional
    public SalesOrder voidOrder(Long id) {
        SalesOrder so = findById(id);
        if (!"COMPLETED".equals(so.getStatus())) {
            throw new BusinessException("Hanya SO berstatus COMPLETED yang dapat di-void");
        }

        Store store = so.getStore();
        Customer customer = so.getCustomer();
        LocalDate txDate = LocalDate.now();

        // 1. Balik stok (item_ledgers)
        for (SalesOrderItem soi : so.getItems()) {
            inventoryLedgerService.postMovement(
                    store, soi.getItem(),
                    "SALE_VOID", so.getOrderNo(), so.getId(),
                    txDate,
                    soi.getQtyMl(), // positif = stok kembali
                    soi.getUnitCost(),
                    "Void penjualan " + so.getOrderNo());
        }

        // 2. Balik AR jika CREDIT
        if ("CREDIT".equals(so.getPaymentMethod()) && customer != null) {
            customerLedgerService.post(
                    store, customer,
                    "SALE_VOID", so.getOrderNo(), so.getId(),
                    txDate,
                    so.getTotalAmount().negate(),
                    "Void penjualan " + so.getOrderNo());
        }

        // 3. Balik GL (reverse entry)
        List<GeneralLedgerService.GlLine> debits = new ArrayList<>();
        List<GeneralLedgerService.GlLine> credits = new ArrayList<>();

        // Kredit: Kas/Bank/Piutang (reversal)
        String creditCoa;
        if ("CREDIT".equals(so.getPaymentMethod())) {
            creditCoa = CoaCode.PIUTANG_USAHA;
        } else if ("QRIS".equals(so.getPaymentMethod())) {
            creditCoa = CoaCode.BANK;
        } else {
            creditCoa = CoaCode.KAS;
        }
        credits.add(new GeneralLedgerService.GlLine(creditCoa, so.getTotalAmount(), "Reversal kas"));

        // Debit: Pendapatan (reversal)
        debits.add(new GeneralLedgerService.GlLine(CoaCode.PENJUALAN, so.getTotalAmount(), "Reversal pendapatan"));

        // Reversal HPP
        BigDecimal totalHpp = so.getItems().stream()
                .map(SalesOrderItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalHpp.compareTo(BigDecimal.ZERO) > 0) {
            credits.add(new GeneralLedgerService.GlLine(CoaCode.HPP, totalHpp, "Reversal HPP"));
            debits.add(new GeneralLedgerService.GlLine(CoaCode.PERSEDIAAN, totalHpp, "Reversal persediaan"));
        }

        glService.postDoubleEntry(
                store, "SALES_VOID", so.getOrderNo(), so.getId(),
                txDate, debits, credits,
                "Void penjualan " + so.getOrderNo());

        so.setStatus("VOID");
        return repo.save(so);
    }

    public SalesOrderResponse toResponse(SalesOrder so) {
        SalesOrderResponse res = new SalesOrderResponse();
        res.setId(so.getId());
        res.setOrderNo(so.getOrderNo());
        res.setOrderDate(so.getOrderDate());
        res.setStoreId(so.getStore().getId());
        res.setStoreName(so.getStore().getName());
        if (so.getCustomer() != null) {
            res.setCustomerId(so.getCustomer().getId());
            res.setCustomerName(so.getCustomer().getName());
        }
        res.setPaymentMethod(so.getPaymentMethod());
        res.setStatus(so.getStatus());
        res.setSubtotal(so.getSubtotal());
        res.setDiscountAmount(so.getDiscountAmount());
        res.setTaxAmount(so.getTaxAmount());
        res.setTotalAmount(so.getTotalAmount());
        res.setPaidAmount(so.getPaidAmount());
        res.setChangeAmount(so.getChangeAmount());
        res.setNotes(so.getNotes());
        res.setCreatedBy(so.getCreatedBy());
        res.setCreatedAt(so.getCreatedAt());
        res.setItems(so.getItems().stream().map(i -> {
            var d = new SalesOrderResponse.ItemLine();
            d.setItemId(i.getItem().getId());
            d.setItemCode(i.getItem().getCode());
            d.setItemName(i.getItem().getName());
            d.setQtyMl(i.getQtyMl());
            d.setUnitPrice(i.getUnitPrice());
            d.setDiscountPercent(i.getDiscountPercent());
            d.setTotalPrice(i.getTotalPrice());
            d.setUnitCost(i.getUnitCost());
            d.setTotalCost(i.getTotalCost());
            return d;
        }).toList());
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

    /**
     * Cari SO credit yang masih outstanding untuk customer tertentu.
     * Digunakan di form Customer Payment untuk dropdown SO.
     */
    public List<SalesOrderResponse> findOpenByCustomer(Long customerId, Long storeId) {
        if (storeId == null && !SecurityUtils.isSuperAdmin()) {
            storeId = storeContext.getCurrentStoreId();
        }
        return repo.findOpenByCustomer(customerId, storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}