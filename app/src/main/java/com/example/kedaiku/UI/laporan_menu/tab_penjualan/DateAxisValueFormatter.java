package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {
    private long baseTimestamp; // misalnya, awal bulan dalam milidetik
    private SimpleDateFormat dateFormat;

    public DateAxisValueFormatter(long baseTimestamp) {
        this.baseTimestamp = baseTimestamp;
        // Format yang diinginkan, misalnya "dd/MM"
        this.dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        // Asumsikan value adalah offset hari; hitung timestamp-nya
        long timestamp = baseTimestamp + ((long) value * 86400000L);
        return dateFormat.format(new Date(timestamp));
    }
}
