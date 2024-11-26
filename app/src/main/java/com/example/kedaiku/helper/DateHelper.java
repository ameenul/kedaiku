package com.example.kedaiku.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public  class DateHelper {


    public static long[] calculateDateRange(String filterName) {
        long startDate, endDate;

        switch (filterName) {
            case "Hari Ini":
                startDate = getStartOfDay();
                endDate = getEndOfDay();//
                break;
            case "Kemarin":
                startDate = getStartOfYesterday();
                endDate = getStartOfDay();
                break;
            case "Bulan Ini":
                startDate = getStartOfMonth();
                endDate = getEndOfDay();//
                break;
            case "Bulan Lalu":
                startDate = getStartOfLastMonth();
                endDate = getStartOfMonth();
                break;
            default:
                startDate = 0;
                endDate = getEndOfDay();//
                break;
        }

        return new long[]{startDate, endDate};
    }

    // Helper methods untuk mendapatkan waktu tertentu
    private static long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getStartOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getStartOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getEndOfDay() {

        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        day.set(Calendar.MILLISECOND, 999);
        return day.getTimeInMillis();
    }

    public static String getDescDate(long date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }

    public static String getDescStartEndDate(long[] date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        String descStartDate = dateFormat.format(date[0]);
        String descEndDate = dateFormat.format(date[1]);

        return descStartDate+ " s.d "+descEndDate;

    }





}
