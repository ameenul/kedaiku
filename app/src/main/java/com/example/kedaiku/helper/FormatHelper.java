package com.example.kedaiku.helper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FormatHelper {


    public static String formatCurrency(double value) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return numberFormat.format(value);
    }


    public static String getDescDate(long date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }


    public static String getDescDate(long date, String format)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }
}
