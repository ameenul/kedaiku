package com.example.kedaiku.UI.cash_menu;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.viewmodel.CashFlowViewModel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CashFlowActivity extends AppCompatActivity {

    private CashFlowViewModel cashFlowViewModel;
    private RecyclerView recyclerView;
    private CashFlowAdapter adapter;
    private long cashId;
    private String cashName;
    private TextView textViewEmptyMessage;

    private long startDateInMillis = 0;
    private long endDateInMillis = 0;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private TextView textViewSelectedDateRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow);

        recyclerView = findViewById(R.id.recyclerViewCashFlow);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        textViewEmptyMessage = findViewById(R.id.textViewEmptyMessage);
        textViewSelectedDateRange = findViewById(R.id.textViewSelectedDateRange); // Inisialisasi


        adapter = new CashFlowAdapter(this);
        recyclerView.setAdapter(adapter);

        cashId = getIntent().getIntExtra("cash_id", -1);
        cashName = getIntent().getStringExtra("cash_name");

        Log.d("CashFlowActivity", "Received cashId: " + cashId + ", cashName: " + cashName);

        if (cashId == -1) {
            Toast.makeText(this, "Invalid cash ID"+cashId, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Cash Flow - " + cashName);

        cashFlowViewModel = new ViewModelProvider(this).get(CashFlowViewModel.class);
        loadCashFlowData();
    }

//    private void loadCashFlowData() {
//        if (startDateInMillis > 0 && endDateInMillis > 0) {
//            cashFlowViewModel.getCashFlowByCashIdAndDateRange(cashId, startDateInMillis, endDateInMillis).observe(this, cashFlows -> {
//                updateUI(cashFlows);
//            });
//        } else {
//            cashFlowViewModel.getCashFlowByCashId(cashId).observe(this, cashFlows -> {
//                updateUI(cashFlows);
//            });
//        }
//    }

    private void loadCashFlowData() {
        if (startDateInMillis > 0 && endDateInMillis > 0) {
            // Menampilkan rentang tanggal yang dipilih
            String startDateStr = dateFormat.format(startDateInMillis);
            String endDateStr = dateFormat.format(endDateInMillis);
            textViewSelectedDateRange.setText("Periode: " + startDateStr + " - " + endDateStr);
            textViewSelectedDateRange.setVisibility(View.VISIBLE);

            cashFlowViewModel.getCashFlowByCashIdAndDateRange(cashId, startDateInMillis, endDateInMillis).observe(this, cashFlows -> {
                updateUI(cashFlows);
            });
        } else {
            // Sembunyikan textViewSelectedDateRange
            textViewSelectedDateRange.setVisibility(View.GONE);

            cashFlowViewModel.getCashFlowByCashId(cashId).observe(this, cashFlows -> {
                updateUI(cashFlows);
            });
        }
    }

//    private void updateUI(List<CashFlow> cashFlows) {
//        if (cashFlows != null) {
//            Log.d("CashFlowActivity", "Number of cash flows: " + cashFlows.size());
//            for (CashFlow cf : cashFlows) {
//                Log.d("CashFlowActivity", "CashFlow: " + cf.toString());
//            }
//        } else {
//            Log.d("CashFlowActivity", "cashFlows is null");
//        }
//
//        if (cashFlows == null || cashFlows.isEmpty()) {
//            // Tampilkan pesan kosong
//            textViewEmptyMessage.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//        } else {
//            // Sembunyikan pesan kosong
//            textViewEmptyMessage.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//            adapter.setCashFlowList(cashFlows);
//        }
//    }

    private void updateUI(List<CashFlow> cashFlows) {
        if (cashFlows == null || cashFlows.isEmpty()) {
            textViewEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setCashFlowList(cashFlows);
        }
    }

    // Menu untuk filter tanggal
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash_flow, menu);
        return true;
    }


//    public boolean onOptionsItemSelected(MenuItem item) {
//        Calendar calendar = Calendar.getInstance();
//        if (item.getItemId() == R.id.action_filter_today) {
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            startDateInMillis = calendar.getTimeInMillis();
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//            endDateInMillis = calendar.getTimeInMillis();
//
//            loadCashFlowData();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_yesterday) {
//            calendar.add(Calendar.DAY_OF_MONTH, -1);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            startDateInMillis = calendar.getTimeInMillis();
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//            endDateInMillis = calendar.getTimeInMillis();
//
//            loadCashFlowData();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_this_month) {
//            calendar.set(Calendar.DAY_OF_MONTH, 1);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            startDateInMillis = calendar.getTimeInMillis();
//
//            calendar.add(Calendar.MONTH, 1);
//            endDateInMillis = calendar.getTimeInMillis();
//
//            loadCashFlowData();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_last_month) {
//            calendar.add(Calendar.MONTH, -1);
//            calendar.set(Calendar.DAY_OF_MONTH, 1);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            startDateInMillis = calendar.getTimeInMillis();
//
//            calendar.add(Calendar.MONTH, 1);
//            endDateInMillis = calendar.getTimeInMillis();
//
//            loadCashFlowData();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_all_time) {
//            startDateInMillis = 0;
//            endDateInMillis = 0;
//            loadCashFlowData();
//            return true;
//        } else if (item.getItemId() == R.id.action_filter_custom) {
//            showDateRangeDialog();
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }

//    private void showDateRangePicker() {
//        // Implementasi DatePickerDialog untuk memilih tanggal awal dan akhir
//        final Calendar startCalendar = Calendar.getInstance();
//        final Calendar endCalendar = Calendar.getInstance();
//
//        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
//            startDateInMillis = startCalendar.getTimeInMillis();
//
//            DatePickerDialog endDatePicker = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth1) -> {
//                endCalendar.set(year1, month1, dayOfMonth1, 23, 59, 59);
//                endDateInMillis = endCalendar.getTimeInMillis();
//
//                loadCashFlowData();
//            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
//
//            endDatePicker.show();
//        }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
//
//        startDatePicker.show();
//    }

//    private void showDateRangePickerDialog() {
//        final Calendar startCalendar = Calendar.getInstance();
//        final Calendar endCalendar = Calendar.getInstance();
//
//        // DatePickerDialog untuk tanggal awal
//        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
//
//            // Setelah memilih tanggal awal, tampilkan DatePickerDialog untuk tanggal akhir
//            DatePickerDialog endDatePicker = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth1) -> {
//                endCalendar.set(year1, month1, dayOfMonth1, 23, 59, 59);
//
//                // Validasi apakah tanggal akhir setelah atau sama dengan tanggal awal
//                if (endCalendar.getTimeInMillis() >= startCalendar.getTimeInMillis()) {
//                    startDateInMillis = startCalendar.getTimeInMillis();
//                    endDateInMillis = endCalendar.getTimeInMillis();
//
//                    loadCashFlowData();
//                } else {
//                    Toast.makeText(this, "Tanggal akhir harus setelah tanggal awal", Toast.LENGTH_SHORT).show();
//                }
//            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
//
//            endDatePicker.setTitle("Pilih Tanggal Akhir");
//            endDatePicker.show();
//        }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
//
//        startDatePicker.setTitle("Pilih Tanggal Awal");
//        startDatePicker.show();
//    }


    public boolean onOptionsItemSelected(MenuItem item) {
        Calendar calendar = Calendar.getInstance();


            if (item.getItemId() == R.id.action_filter_today) {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDateInMillis = calendar.getTimeInMillis();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDateInMillis = calendar.getTimeInMillis();

                textViewSelectedDateRange.setVisibility(View.GONE);

                loadCashFlowData();
                return true;
            } else if (item.getItemId() == R.id.action_filter_yesterday) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDateInMillis = calendar.getTimeInMillis();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDateInMillis = calendar.getTimeInMillis();

                textViewSelectedDateRange.setVisibility(View.GONE);

                loadCashFlowData();
                return true;
            } else if (item.getItemId() == R.id.action_filter_this_month) {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDateInMillis = calendar.getTimeInMillis();

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDateInMillis = calendar.getTimeInMillis();

                textViewSelectedDateRange.setVisibility(View.GONE);

                loadCashFlowData();
                return true;
            } else if (item.getItemId() == R.id.action_filter_last_month) {
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDateInMillis = calendar.getTimeInMillis();

                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDateInMillis = calendar.getTimeInMillis();

                textViewSelectedDateRange.setVisibility(View.GONE);

                loadCashFlowData();
                return true;
            } else if (item.getItemId() == R.id.action_filter_all_time) {
                startDateInMillis = 0;
                endDateInMillis = 0;

                textViewSelectedDateRange.setVisibility(View.GONE);

                loadCashFlowData();
                return true;
            } else if (item.getItemId() == R.id.action_filter_custom) {
                showDateRangeDialog();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        }


    private void showDateRangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Rentang Tanggal");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_range_picker, null);
        builder.setView(dialogView);

        Button buttonStartDate = dialogView.findViewById(R.id.buttonStartDate);
        Button buttonEndDate = dialogView.findViewById(R.id.buttonEndDate);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();

        // Listener untuk Tanggal Awal
        buttonStartDate.setOnClickListener(v -> {
            int year = startCalendar.get(Calendar.YEAR);
            int month = startCalendar.get(Calendar.MONTH);
            int day = startCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                startCalendar.set(selectedYear, selectedMonth, selectedDay);
                buttonStartDate.setText(dateFormat.format(startCalendar.getTime()));
            }, year, month, day);

            datePickerDialog.show();
        });

        // Listener untuk Tanggal Akhir
        buttonEndDate.setOnClickListener(v -> {
            int year = endCalendar.get(Calendar.YEAR);
            int month = endCalendar.get(Calendar.MONTH);
            int day = endCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                endCalendar.set(selectedYear, selectedMonth, selectedDay);
                buttonEndDate.setText(dateFormat.format(endCalendar.getTime()));
            }, year, month, day);

            datePickerDialog.show();
        });

        builder.setPositiveButton("Filter", (dialog, which) -> {
            String startDateText = buttonStartDate.getText().toString();
            String endDateText = buttonEndDate.getText().toString();

            if (startDateText.equals("Pilih Tanggal Awal") || endDateText.equals("Pilih Tanggal Akhir")) {
                Toast.makeText(this, "Silakan pilih tanggal awal dan akhir", Toast.LENGTH_SHORT).show();
            } else {
                // Validasi apakah tanggal akhir tidak sebelum tanggal awal
                if (endCalendar.getTimeInMillis() >= startCalendar.getTimeInMillis()) {
                    // Atur waktu untuk awal hari dan akhir hari
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);

                    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    endCalendar.set(Calendar.MINUTE, 59);
                    endCalendar.set(Calendar.SECOND, 59);
                    endCalendar.set(Calendar.MILLISECOND, 999);

                    startDateInMillis = startCalendar.getTimeInMillis();
                    endDateInMillis = endCalendar.getTimeInMillis();

                    loadCashFlowData();
                } else {
                    Toast.makeText(this, "Tanggal akhir harus setelah atau sama dengan tanggal awal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }



}
