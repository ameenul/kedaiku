package com.example.kedaiku.UI.laporan_menu.tab_labarugi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;

import com.example.kedaiku.UI.laporan_menu.TanggalJumlahItem;
import com.example.kedaiku.entites.ExpenseWithCash;

import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.ChartDataPoint;
import com.example.kedaiku.viewmodel.ChartFilterParams;
import com.example.kedaiku.viewmodel.ExpenseViewModel;
import com.example.kedaiku.viewmodel.SaleViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LabaRugiFragment extends Fragment {

    private LineChart chartLabaRugi;
    private TextView tvTotalLabaRugi;
    private RecyclerView rvLabaRugiReport;
    private RadioGroup rgFilterGrafik;
    private RadioButton rbBulan, rbTahun;
    private Spinner spinnerFilterOption;
    private Button btnExportLaporanCsv;

    private SaleViewModel saleViewModel;
    private LabaRugiAdapter labaRugiAdapter; // Adapter untuk daftar laporan laba rugi

    private String csvData = "";
    private ActivityResultLauncher<Intent> createFileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laba_rugi, container, false);
        initUI(view);
        setupRecyclerView();
        setupViewModel();
        setupEvents();
        setupFileLauncher();
        return view;
    }

    private void initUI(View view) {
        chartLabaRugi = view.findViewById(R.id.chartLabaRugi);
        tvTotalLabaRugi = view.findViewById(R.id.tvTotalLabaRugi);
        rvLabaRugiReport = view.findViewById(R.id.rvLabaRugiReport);
        rgFilterGrafik = view.findViewById(R.id.rgFilterGrafik);
        rbBulan = view.findViewById(R.id.rbBulan);
        rbTahun = view.findViewById(R.id.rbTahun);
        spinnerFilterOption = view.findViewById(R.id.spinnerFilterOption);
        btnExportLaporanCsv = view.findViewById(R.id.btnExportLaporanCsv);
    }

    private void setupRecyclerView() {
        rvLabaRugiReport.setLayoutManager(new LinearLayoutManager(getContext()));
        labaRugiAdapter = new LabaRugiAdapter(new ArrayList<>());
        rvLabaRugiReport.setAdapter(labaRugiAdapter);
    }

    private void setupViewModel() {
        saleViewModel = new ViewModelProvider(requireActivity()).get(SaleViewModel.class);
        // Observasi data grafik expense (menggunakan filter grafik di SaleViewModel)
        saleViewModel.getChartData().observe(getViewLifecycleOwner(), chartDataPoints -> {
            updateLineChart(chartDataPoints);
        });
        // Observasi data expense untuk laporan (menggunakan filteredExpenseForChart)
        saleViewModel.getFilteredSalesForChart().observe(getViewLifecycleOwner(), saleList -> {
            updateExpenseSummary(saleList);
            List<TanggalJumlahItem> aggregatedData = aggregateExpenseData(saleList);
            labaRugiAdapter.updateData(aggregatedData);
        });
    }

    private void setupEvents() {
        // RadioGroup listener untuk filter grafik
        rgFilterGrafik.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBulan) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.filter_options_bulan, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterOption.setAdapter(adapter);
                saleViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
            } else if (checkedId == R.id.rbTahun) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.filter_options_tahun, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterOption.setAdapter(adapter);
                saleViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
            }
        });

        // Spinner listener untuk opsi filter grafik
        spinnerFilterOption.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String option = parent.getItemAtPosition(position).toString();
                if (rbBulan.isChecked() && option.equalsIgnoreCase("Bulan ini")) {
                    saleViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
                } else if (rbTahun.isChecked() && option.equalsIgnoreCase("Tahun ini")) {
                    saleViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
                } else if (option.equalsIgnoreCase("Custom")) {
                    showCustomChartFilterDialog(rbBulan.isChecked() ? "Bulan" : "Tahun");
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Button export CSV
        btnExportLaporanCsv.setOnClickListener(v -> exportDataToCsv());
    }

    private void setupFileLauncher() {
        createFileLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            writeCsvDataToUri(uri);
                        }
                    }
                }
        );
    }

    private void updateLineChart(List<ChartDataPoint> chartDataPoints) {
        List<Entry> entries = new ArrayList<>();
        int index = 0;
        for (ChartDataPoint point : chartDataPoints) {
            // Gunakan index sebagai nilai x dan value sebagai y
            entries.add(new Entry(index, (float) point.getValue()));
            index++;
        }
        LineDataSet dataSet = new LineDataSet(entries, "Laba Rugi");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextColor(Color.WHITE);
        // Atur style garis sesuai kebutuhan (misal, disable circle atau enable circle)
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.WHITE);
        LineData lineData = new LineData(dataSet);
        chartLabaRugi.setData(lineData);
        // Konfigurasi sumbu dan legend
        chartLabaRugi.getXAxis().setDrawLabels(false);
        chartLabaRugi.getAxisLeft().setTextColor(Color.WHITE);
        chartLabaRugi.getAxisRight().setTextColor(Color.WHITE);
        chartLabaRugi.getDescription().setEnabled(false);
        Legend legend = chartLabaRugi.getLegend();
        legend.setTextColor(Color.WHITE);
        chartLabaRugi.invalidate();
    }

    private void updateExpenseSummary(List<SaleWithDetails> saleList) {
        double totalLaba = 0;
        for (SaleWithDetails ewc : saleList) {
            //totalExpense += ewc.getExpense().getExpense_amount();
        }
        // Misalnya, jika laba rugi dihitung sebagai (pendapatan - pengeluaran),
        // Anda dapat menyesuaikan perhitungan di sini. Untuk contoh, kita tampilkan total pengeluaran sebagai "Laba Rugi"
        tvTotalLabaRugi.setText("Laba Rugi: " + FormatHelper.formatCurrency(totalLaba));
    }

    private List<TanggalJumlahItem> aggregateExpenseData(List<SaleWithDetails> saleList) {
        // Agregasi data per tanggal (format "dd/MM/yyyy")
        java.util.Map<String, Double> dateToTotal = new java.util.TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (SaleWithDetails sale : saleList) {
            String dateStr = sdf.format(new Date(sale.getSaleDate()));
            double current = dateToTotal.getOrDefault(dateStr, 0.0);
            dateToTotal.put(dateStr, current + sale.getSaleTotal());
        }
        List<TanggalJumlahItem> result = new ArrayList<>();
        for (java.util.Map.Entry<String, Double> entry : dateToTotal.entrySet()) {
            result.add(new TanggalJumlahItem(entry.getKey(), FormatHelper.formatCurrency(entry.getValue())));
        }
        return result;
    }

    private void showCustomChartFilterDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (type.equals("Bulan")) {
            builder.setTitle("Masukkan Tahun (yyyy) dan Bulan (mm)");
        } else {
            builder.setTitle("Masukkan Tahun (yyyy)");
        }
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText etYear = new EditText(getContext());
        etYear.setHint("Tahun (yyyy)");
        etYear.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etYear);

        if (type.equals("Bulan")) {
            final EditText etMonth = new EditText(getContext());
            etMonth.setHint("Bulan (mm)");
            etMonth.setInputType(InputType.TYPE_CLASS_NUMBER);
            layout.addView(etMonth);
            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String yearStr = etYear.getText().toString().trim();
                String monthStr = etMonth.getText().toString().trim();
                if (!yearStr.isEmpty() && !monthStr.isEmpty()) {
                    int year = Integer.parseInt(yearStr);
                    int month = Integer.parseInt(monthStr);
                    long start = DateHelper.getStartOfMonth(year, month);
                    long end = DateHelper.getEndOfMonth(year, month);
                    saleViewModel.setChartFilter("Bulan", start, end);
                } else {
                    Toast.makeText(getContext(), "Harap masukkan tahun dan bulan", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String yearStr = etYear.getText().toString().trim();
                if (!yearStr.isEmpty()) {
                    int year = Integer.parseInt(yearStr);
                    long start = DateHelper.getStartOfYear(year);
                    long end = DateHelper.getEndOfYear(year);
                    saleViewModel.setChartFilter("Tahun", start, end);
                } else {
                    Toast.makeText(getContext(), "Harap masukkan tahun", Toast.LENGTH_SHORT).show();
                }
            });
        }
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void exportDataToCsv() {
        List<TanggalJumlahItem> dataList = labaRugiAdapter.getDataList();
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] headers = {"Tanggal", "Total Pengeluaran"};
        List<String[]> rows = new ArrayList<>();
        for (TanggalJumlahItem item : dataList) {
            String[] row = {item.getTanggal(), item.getJumlah()};
            rows.add(row);
        }
        String judul = "";
        ChartFilterParams chartParams = saleViewModel.getCurrentChartFilterParams();
        if (chartParams != null && chartParams.startDate != null && chartParams.endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startStr = sdf.format(new Date(chartParams.startDate));
            String endStr = sdf.format(new Date(chartParams.endDate));
            judul = "Laporan Laba Rugi (" + startStr + " - " + endStr + ")\n";
        } else {
            judul = "Laporan Laba Rugi\n";
        }
        judul += "\n" + tvTotalLabaRugi.getText().toString() + "\n";
        csvData = com.example.kedaiku.helper.CsvHelper.convertToCsv(headers, rows, judul);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "laporan_laba_rugi.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            java.io.OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    
}
