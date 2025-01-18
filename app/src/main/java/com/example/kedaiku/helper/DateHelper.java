package com.example.kedaiku.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kedaiku.R;
import com.example.kedaiku.viewmodel.DateRangeFilter;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public  class DateHelper {


    public String filter,lastFilter;

    public DateHelper() {
        filter = "Semua Waktu";
        lastFilter = "Semua Waktu";
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


    public AdapterView.OnItemSelectedListener spinnerSelectedListener(Context context, DateRangeFilter ifaceDateRange, StringBuilder dateRange, TextView textViewSelectedDates, Spinner spinnerFilter) {
        WeakReference<Context> contextRef = new WeakReference<>(context);

        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Context ctx = contextRef.get();
                if (ctx == null) return; // Jika context sudah dihancurkan, keluar dari fungsi

                filter = parent.getItemAtPosition(position).toString();

                if ("Pilih Tanggal".equals(filter)) {
                    lastFilter = filter;
                    showDateRangePicker(ctx, ifaceDateRange, dateRange, textViewSelectedDates, spinnerFilter);

                } else if (!ctx.getResources().getStringArray(R.array.filter_options)[0].equals(filter)) {
                    ifaceDateRange.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter);
                    lastFilter = filter;
                    spinnerFilter.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }







}
