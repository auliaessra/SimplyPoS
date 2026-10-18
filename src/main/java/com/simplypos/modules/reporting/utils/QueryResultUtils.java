package com.simplypos.modules.reporting.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Helper untuk handle konversi tipe hasil native query.
 * Hibernate 6 mengembalikan java.time.LocalDate/LocalDateTime untuk
 * kolom DATE/TIMESTAMP, sedangkan Hibernate 5 mengembalikan
 * java.sql.Date/Timestamp.
 * Helper ini aman untuk keduanya.
 */
public final class QueryResultUtils {

    private QueryResultUtils() {
    }

    /** Konversi objek hasil query ke LocalDate */
    public static LocalDate toLocalDate(Object value) {
        if (value == null)
            return null;
        if (value instanceof LocalDate ld)
            return ld;
        if (value instanceof java.sql.Date sd)
            return sd.toLocalDate();
        if (value instanceof java.util.Date ud) {
            return new java.sql.Date(ud.getTime()).toLocalDate();
        }
        if (value instanceof LocalDateTime ldt)
            return ldt.toLocalDate();
        if (value instanceof Timestamp ts)
            return ts.toLocalDateTime().toLocalDate();
        throw new IllegalArgumentException(
                "Cannot convert to LocalDate: " + value.getClass().getName());
    }

    /** Konversi objek hasil query ke LocalDateTime */
    public static LocalDateTime toLocalDateTime(Object value) {
        if (value == null)
            return null;
        if (value instanceof LocalDateTime ldt)
            return ldt;
        if (value instanceof Timestamp ts)
            return ts.toLocalDateTime();
        if (value instanceof LocalDate ld)
            return ld.atStartOfDay();
        if (value instanceof java.sql.Date sd)
            return sd.toLocalDate().atStartOfDay();
        if (value instanceof java.util.Date ud) {
            return new Timestamp(ud.getTime()).toLocalDateTime();
        }
        throw new IllegalArgumentException(
                "Cannot convert to LocalDateTime: " + value.getClass().getName());
    }

    /** Konversi objek hasil query ke YearMonth */
    public static YearMonth toYearMonth(Object value) {
        if (value == null)
            return null;
        if (value instanceof YearMonth ym)
            return ym;
        LocalDateTime ldt = toLocalDateTime(value);
        return YearMonth.from(ldt);
    }
}