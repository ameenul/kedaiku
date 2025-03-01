package com.example.kedaiku.helper;

public interface DateRangeCallback {
    /**
     * Dipanggil saat user telah memilih rentang tanggal (startMillis <= endMillis).
     */
    void onDateRangeSelected(long startMillis, long endMillis);

    /**
     * Dipanggil jika user membatalkan pemilihan tanggal,
     * atau end date < start date, dsb.
     */
    void onDateRangeCanceled();
}
