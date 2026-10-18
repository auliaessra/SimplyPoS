package com.simplypos.modules.finance.service;

/**
 * Mapping kode akun standar Simply PoS.
 * Digunakan untuk auto-posting GL.
 */
public final class CoaCode {

    private CoaCode() {
    }

    // ASSET
    public static final String KAS = "1101";
    public static final String BANK = "1102";
    public static final String PIUTANG_USAHA = "1201";
    public static final String PERSEDIAAN = "1301";

    // LIABILITY
    public static final String HUTANG_USAHA = "2101";
    public static final String HUTANG_PAJAK = "2102";

    // EQUITY
    public static final String MODAL = "3101";

    // REVENUE
    public static final String PENJUALAN = "4101";

    // EXPENSE
    public static final String HPP = "5101";
}