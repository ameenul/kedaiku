package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

import com.example.kedaiku.helper.FormatHelper;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class BarValueFormatter extends ValueFormatter {
//    @Override
//    public String getBarLabel(BarEntry entry) {
//        if (entry.getData() != null) {
//            return entry.getData().toString();
//        } else {
//            return "";
//        }
//    }

    @Override
    public String getBarLabel(BarEntry entry) {
        // Ambil label tanggal dari properti data BarEntry
        String dateLabel = entry.getData() != null ? entry.getData().toString() : "";
        // Format nilai penjualan menggunakan FormatHelper (misalnya, format currency)
        String salesValue = FormatHelper.formatCurrency(entry.getY());
        // Gabungkan keduanya, misalnya dengan pemisah baris ("\n")
        return dateLabel + " : " + salesValue;
    }




}
