package com.example.kedaiku.helper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kedaiku.R;
import com.example.kedaiku.viewmodel.DateRangeFilter;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public  class DateHelper {


    public String filter,lastFilter;
    boolean isFirst;

    public static long parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : -1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public DateHelper() {
        filter = "Semua Waktu";
        lastFilter = "Semua Waktu";
        isFirst=true;
    }

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
    public static long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getStartOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfMonth() {
        Calendar cal = Calendar.getInstance();
        // Set ke hari terakhir bulan berjalan
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        // Set ke waktu akhir hari
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static long getStartOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfDay() {

        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        day.set(Calendar.MILLISECOND, 999);
        return day.getTimeInMillis();
    }


    public static long getStartOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, 0); // Januari (0)
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, 11); // Desember (11)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }


    /**
     * Mendapatkan timestamp untuk awal bulan tertentu dalam suatu tahun.
     *
     * @param year  Tahun, misalnya 2025.
     * @param month Bulan (1-12); perhatikan bahwa Calendar menggunakan indeks 0 untuk Januari,
     *              sehingga nilai month yang diberikan harus dikurangi 1.
     * @return Timestamp (milidetik) pada awal bulan tersebut (tanggal 1 jam 00:00:00.000).
     */
    public static long getStartOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // Set ke tahun, bulan (dikurangi 1) dan tanggal 1
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Mendapatkan timestamp untuk akhir bulan tertentu dalam suatu tahun.
     *
     * @param year  Tahun, misalnya 2025.
     * @param month Bulan (1-12); perhatikan bahwa Calendar menggunakan indeks 0 untuk Januari.
     * @return Timestamp (milidetik) pada akhir bulan tersebut (tanggal terakhir jam 23:59:59.999).
     */
    public static long getEndOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // Set ke awal bulan terlebih dahulu
        cal.set(year, month - 1, 1, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        // Ubah ke hari terakhir bulan tersebut
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

    /**
     * Mengembalikan rentang waktu (start dan end) untuk bulan tertentu dalam suatu tahun.
     *
     * @param year  Tahun, misalnya 2025.
     * @param month Bulan (1-12).
     * @return Array dengan dua elemen: [0] = start timestamp, [1] = end timestamp.
     */
    public static long[] getMonthRange(int year, int month) {
        return new long[]{ getStartOfMonth(year, month), getEndOfMonth(year, month) };
    }

    public static long[] fixmmssRange(Calendar start,Calendar end)
    {
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

                    long startDate = start.getTimeInMillis();
                    long endDate = end.getTimeInMillis();
        return new long[]{startDate, endDate};

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

    public static String getDescStartEndDateShort(long[] date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String descStartDate = dateFormat.format(date[0]);
        String descEndDate = dateFormat.format(date[1]);

        return descStartDate+ " s.d "+descEndDate;

    }

    public void showDateRangePicker(Context context, DateRangeFilter ifaceDateRange, StringBuilder text, TextView textViewSelectedDates, Spinner spinnerFilter)
    {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog startDatePicker = new DatePickerDialog(
                context,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.set(year, month, dayOfMonth);
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    long startDate = startCalendar.getTimeInMillis();

                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            context,
                            (DatePicker endView, int endYear, int endMonth, int endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth);
                                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                                endCalendar.set(Calendar.MINUTE, 59);
                                endCalendar.set(Calendar.SECOND, 59);
                                endCalendar.set(Calendar.MILLISECOND, 999);
                                long endDate = endCalendar.getTimeInMillis();


                                ifaceDateRange.setDateRangeFilter(startDate, endDate);

                                // Format the selected dates
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
                                String dateRangeText = "Tanggal Terpilih: " +
                                        dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
                                text.setLength(0);
                                text.append(dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime()));
                                textViewSelectedDates.setText(dateRangeText);
                                spinnerFilter.setSelection(0);


                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    endDatePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        startDatePicker.show();
    }


    public AdapterView.OnItemSelectedListener
    spinnerSelectedListener(Context context, DateRangeFilter ifaceDateRange,
                            StringBuilder dateRange, TextView textViewSelectedDates, Spinner spinnerFilter)
    {
        WeakReference<Context> contextRef = new WeakReference<>(context);

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Context ctx = contextRef.get();
                if (ctx == null) return; // Jika context sudah dihancurkan, keluar dari fungsi

                //filter = parent.getItemAtPosition(position).toString();
                filter = ctx.getResources().getStringArray(R.array.filter_options)[position];

                if ("Pilih Tanggal".equals(filter)) {
                    lastFilter = filter;
                    showDateRangePicker(ctx, ifaceDateRange, dateRange, textViewSelectedDates, spinnerFilter);

                }  else if(position==0) {

                    if(isFirst)
                    {
//                        Toast.makeText(getContext(), "masuk: " + position, Toast.LENGTH_SHORT).show();
                        isFirst=false;
                        lastFilter = "Semua Waktu";
                        ifaceDateRange.setFilter(lastFilter);
                        textViewSelectedDates.setText("Tanggal Terpilih: " + lastFilter + " (Klik Tiap Item Untuk Detail)");
                    }

                }

                else{
                    ifaceDateRange.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter);
                    lastFilter = filter;


                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }


    public static void pickCustomDateRange(Activity activity, DateRangeCallback callback) {
        // 1. Minta start date
        final Calendar calendar = Calendar.getInstance();
        int defaultYear = calendar.get(Calendar.YEAR);
        int defaultMonth = calendar.get(Calendar.MONTH);
        int defaultDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDialog = new DatePickerDialog(
                activity,
                (view, year, month, dayOfMonth) -> {
                    // Simpan start date
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(Calendar.YEAR, year);
                    startCal.set(Calendar.MONTH, month);
                    startCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    startCal.set(Calendar.HOUR_OF_DAY, 0);
                    startCal.set(Calendar.MINUTE, 0);
                    startCal.set(Calendar.SECOND, 0);
                    startCal.set(Calendar.MILLISECOND, 0);
                    long startMillis = startCal.getTimeInMillis();

                    // Setelah user pilih start date, minta end date
                    pickEndDate(activity, callback, startMillis);

                },
                defaultYear,
                defaultMonth,
                defaultDay
        );

        startDialog.show();
    }

    private static void pickEndDate(Activity activity, DateRangeCallback callback, long startMillis) {
        final Calendar calendar = Calendar.getInstance();
        int defaultYear = calendar.get(Calendar.YEAR);
        int defaultMonth = calendar.get(Calendar.MONTH);
        int defaultDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDialog = new DatePickerDialog(
                activity,
                (view, year, month, dayOfMonth) -> {
                    // Simpan end date
                    Calendar endCal = Calendar.getInstance();
                    endCal.set(Calendar.YEAR, year);
                    endCal.set(Calendar.MONTH, month);
                    endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                    endCal.set(Calendar.MINUTE, 59);
                    endCal.set(Calendar.SECOND, 59);
                    endCal.set(Calendar.MILLISECOND, 999);
                    long endMillis = endCal.getTimeInMillis();

                    if (endMillis < startMillis) {
                        // Batal/kesalahan
                        Toast.makeText(activity,
                                "end date tidak boleh lebih kecil dari start date",
                                Toast.LENGTH_SHORT).show();

                        if (callback != null) {
                            callback.onDateRangeCanceled();
                        }
                        return;
                    }

                    // Berhasil
                    if (callback != null) {
                        callback.onDateRangeSelected(startMillis, endMillis);
                    }

                },
                defaultYear,
                defaultMonth,
                defaultDay
        );

        endDialog.show();
    }

    public static long getStartOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0); // Januari
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 11); // Desember
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }



}


