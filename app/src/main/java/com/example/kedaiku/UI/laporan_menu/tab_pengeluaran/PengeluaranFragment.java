package com.example.kedaiku.UI.laporan_menu.tab_pengeluaran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.example.kedaiku.UI.laporan_menu.tab_penjualan.BarValueFormatter;
import com.example.kedaiku.entites.ExpenseWithCash;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.ChartFilterParams;
import com.example.kedaiku.viewmodel.ExpenseViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PengeluaranFragment extends Fragment {

    private BarChart chartExpense;
    private TextView tvTotalExpense;
    private RecyclerView rvExpenseReport;
    private ExpenseViewModel expenseViewModel;
    private TanggalJumlahExpenseAdapter expenseReportAdapter; // Adapter untuk daftar pengeluaran teragregasi

    private Button btnExportLaporanCsv;
    private RadioGroup rgFilterGrafik;
    private RadioButton rbBulan, rbTahun;
    private Spinner spinnerFilterOption;
    private DateHelper dateHelper;
    private StringBuilder dateRange;
    private ActivityResultLauncher<Intent> createFileLauncher;
    double totalChart;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pengeluaran, container, false);
        chartExpense = view.findViewById(R.id.chartExpense);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);

        btnExportLaporanCsv = view.findViewById(R.id.btnExportLaporanCsv);

        rvExpenseReport = view.findViewById(R.id.rvExpenseReport);
        rgFilterGrafik = view.findViewById(R.id.rgFilterGrafik);
        rbBulan = view.findViewById(R.id.rbBulan);
        rbTahun = view.findViewById(R.id.rbTahun);
        spinnerFilterOption = view.findViewById(R.id.spinnerFilterOption);
        dateHelper = new DateHelper();
        dateRange = new StringBuilder();

        setupRecyclerView();
        setupViewModel();
        setupEvents();
        return view;
    }

    private boolean isSpinnerChartInitialized = false; // Flag untuk menghindari trigger awal
    private void setupEvents() {
        // Listener untuk RadioGroup agar opsi Spinner berubah
        rgFilterGrafik.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBulan) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.filter_options_bulan, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterOption.setAdapter(adapter);
                expenseViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
            } else if (checkedId == R.id.rbTahun) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.filter_options_tahun, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterOption.setAdapter(adapter);
                expenseViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
            }
            // Reset flag jika radio group berubah
            isSpinnerChartInitialized = false;
        });

        // Listener untuk spinner filter chart
        spinnerFilterOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Jika belum diinisialisasi (trigger pertama), set flag dan return
                if (!isSpinnerChartInitialized) {
                    isSpinnerChartInitialized = true;
                    return;
                }
                String option = parent.getItemAtPosition(position).toString();
                if (option.equalsIgnoreCase("Custom")) {
                    if (rbBulan.isChecked()) {
                        showCustomChartFilterDialog("Bulan");
                    } else if (rbTahun.isChecked()) {
                        showCustomChartFilterDialog("Tahun");
                    }
                    parent.setSelection(0);
                } else {
                    if (rbBulan.isChecked() && option.equalsIgnoreCase("Bulan ini")) {
                        expenseViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
                    } else if (rbTahun.isChecked() && option.equalsIgnoreCase("Tahun ini")) {
                        expenseViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Tidak ada aksi
            }
        });


        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
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

        btnExportLaporanCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportDataToCsv();
            }
        });

    }

    private void setupRecyclerView() {
        rvExpenseReport.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseReportAdapter = new TanggalJumlahExpenseAdapter(new ArrayList<>());
        rvExpenseReport.setAdapter(expenseReportAdapter);
    }

    private void setupViewModel() {
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        expenseViewModel.getChartData().observe(getViewLifecycleOwner(), chartDataPoints -> {
            updateBarChart(chartDataPoints);
            List<TanggalJumlahItem> aggregatedData = new ArrayList<>();
            for (com.example.kedaiku.viewmodel.ChartDataPoint point : chartDataPoints) {
                aggregatedData.add(new TanggalJumlahItem(point.getLabel(), FormatHelper.formatCurrency(point.getValue())));
            }
            expenseReportAdapter.updateData(aggregatedData);
        });

        expenseViewModel.getFilteredExpenseForChart().observe(getViewLifecycleOwner(), salesListForChart -> {
            updateExpenseSummary(salesListForChart);
        });


        // Asumsikan expenseViewModel.getFilteredExpenses() mengembalikan data expense yang sudah difilter
//        expenseViewModel.getFilteredExpenses().observe(getViewLifecycleOwner(), expenseList -> {
//            // Update summary dan grafik berdasarkan expenseList
//            updateExpenseSummary(expenseList);
//            updateBarChart(expenseList);
//            // Update adapter daftar pengeluaran
//            List<TanggalJumlahItem> aggregatedData = aggregateExpenseData(expenseList);
//            expenseReportAdapter.updateData(aggregatedData);
//        });
    }

    private void updateExpenseSummary(List<ExpenseWithCash> expenseList) {
        double totalExpense = 0;
        double totalRefund = 0;
        // Misal: totalRefund bisa dihitung berdasarkan kategori tertentu atau logika khusus.
        // Di sini, kita asumsikan refund adalah pengeluaran yang ditandai dengan deskripsi tertentu (contoh sederhana: jika expense_name mengandung "Refund")
        for (ExpenseWithCash ewc : expenseList) {
            double amount = ewc.getExpense().getExpense_amount();
            totalExpense += amount;
            if (ewc.getExpense().getExpense_name().toLowerCase(Locale.getDefault()).contains("refund")) {
                totalRefund += amount;
            }
        }
        totalChart = totalExpense;
        tvTotalExpense.setText("Total Pengeluaran: " + FormatHelper.formatCurrency(totalExpense));

    }

    private void updateBarChart(List<com.example.kedaiku.viewmodel.ChartDataPoint> chartDataPoints) {
        List<BarEntry> entries = new ArrayList<>();
        // Misalnya, kita asumsikan bahwa ChartDataPoint.getValue() adalah total penjualan
        // dan ChartDataPoint.getLabel() adalah string tanggal (misalnya "03/01/2025").
        // Untuk sumbu x, kita bisa menggunakan offset hari (misalnya, 0, 1, 2, ...) atau nilai lain.
        int index = 0;
        for (com.example.kedaiku.viewmodel.ChartDataPoint point : chartDataPoints) {
            // Gunakan index sebagai nilai x (agar urutan tetap)
            BarEntry entry = new BarEntry(index, (float) point.getValue());
            // Simpan label tanggal di properti data
            entry.setData(point.getLabel());
            entries.add(entry);
            index++;
        }
        //  Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Jika perlu mengurutkan berdasarkan tanggal, gunakan helper parseTimestamp
        Collections.sort(entries, (e1, e2) -> {
            // Ambil label dari masing-masing entry
            String label1 = e1.getData() != null ? e1.getData().toString() : "";
            String label2 = e2.getData() != null ? e2.getData().toString() : "";
            // Parse ke timestamp menggunakan DateHelper.parseTimestamp
            long t1 = DateHelper.parseTimestamp(label1);
            long t2 = DateHelper.parseTimestamp(label2);
            return Long.compare(t1, t2);
        });

        // Buat dataset dengan satu warna (misalnya biru)
        BarDataSet dataSet = new BarDataSet(entries, "Tanggal");
        dataSet.setColor(Color.BLUE); // Semua batang berwarna biru
        dataSet.setValueTextColor(Color.WHITE);  // Teks nilai (yang digantikan formatter) putih
        dataSet.setValueFormatter(new BarValueFormatter()); // Pasang custom formatter

        BarData data = new BarData(dataSet);
        chartExpense.setData(data);

        // Nonaktifkan label pada sumbu X
        chartExpense.getXAxis().setDrawLabels(false);
        chartExpense.getAxisLeft().setTextColor(Color.WHITE);
        chartExpense.getAxisRight().setTextColor(Color.WHITE);
        chartExpense.getDescription().setEnabled(false);
        chartExpense.getLegend().setTextColor(Color.WHITE);

        chartExpense.invalidate();
    }

    // Contoh agregasi data expense ke dalam list TanggalJumlahExpense (model untuk adapter)
    private List<TanggalJumlahItem> aggregateExpenseData(List<ExpenseWithCash> expenseList) {
        java.util.Map<String, Double> dateToTotal = new java.util.TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (ExpenseWithCash ewc : expenseList) {
            String dateStr = sdf.format(new Date(ewc.getExpense().getExpense_date()));
            double total = dateToTotal.getOrDefault(dateStr, 0.0);
            dateToTotal.put(dateStr, total + ewc.getExpense().getExpense_amount());
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
        final EditText etYear = new EditText(getContext());
        etYear.setHint("Tahun (yyyy)");
        etYear.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etYear);
        final EditText etMonth = null;
        if (type.equals("Bulan")) {
            final EditText etMonthLocal = new EditText(getContext());
            etMonthLocal.setHint("Bulan (mm)");
            etMonthLocal.setInputType(InputType.TYPE_CLASS_NUMBER);
            layout.addView(etMonthLocal);
            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String yearStr = etYear.getText().toString().trim();
                String monthStr = etMonthLocal.getText().toString().trim();
                if (!yearStr.isEmpty() && !monthStr.isEmpty()) {
                    int year = Integer.parseInt(yearStr);
                    int month = Integer.parseInt(monthStr);
                    long start = DateHelper.getStartOfMonth(year, month);
                    long end = DateHelper.getEndOfMonth(year, month);
                    expenseViewModel.setChartFilter("Bulan", start, end);
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
                    expenseViewModel.setChartFilter("Tahun", start, end);
                } else {
                    Toast.makeText(getContext(), "Harap masukkan tahun", Toast.LENGTH_SHORT).show();
                }
            });
        }
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void exportDataToCsv() {
        // Ambil data dari adapter tanggalJumlahAdapter
        List<TanggalJumlahItem> dataList = expenseReportAdapter.getDataList();
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Definisikan header CSV
        String[] headers = {"Tanggal", "Total Pengeluaran"};

        // Bangun list baris data CSV dari dataList adapter
        List<String[]> rows = new ArrayList<>();
        for (TanggalJumlahItem item : dataList) {
            String[] row = { item.getTanggal(), item.getJumlah() };
            rows.add(row);
        }

        // Buat judul CSV dengan menyertakan range tanggal (jika ada)
        String judul = "";
        ChartFilterParams chartParams = expenseViewModel.getCurrentChartFilterParams();
        if (chartParams != null && chartParams.startDate != null && chartParams.endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startStr = sdf.format(new Date(chartParams.startDate));
            String endStr = sdf.format(new Date(chartParams.endDate));
            judul = "Laporan Penjualan (" + startStr + " - " + endStr + ")\n";
        } else {
            judul = "Laporan Penjualan\n";
        }

        // Konversi data ke CSV menggunakan CsvHelper
        String csvData = CsvHelper.convertToCsv(headers, rows, judul);

        // Tambahkan baris summary di akhir: total penjualan, terbayar, dan belum terbayar.
        // Misalnya, TextView summary sudah diupdate sehingga teksnya sudah sesuai.
        csvData += "\n"
                + "Total Pengeluaran: Rp. "+totalChart;

        // Simpan CSV data ke variabel global agar dapat digunakan pada writeCsvDataToUri()
        this.csvData = csvData;

        // Gunakan intent ACTION_CREATE_DOCUMENT untuk membuat file CSV
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "laporan_pengeluaran.csv");
        createFileLauncher.launch(intent);
    }

    private String csvData = "";

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
