package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

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
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.viewmodel.ChartFilterParams;
import com.example.kedaiku.viewmodel.SaleViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PenjualanFragment extends Fragment {

    // Deklarasi view dari fragment_penjualan.xml
    private Spinner spinnerFilterDate;
    private Button btnExportCsv,btnExportLaporanCsv;
    private TextView selectedDate;
    private TextView tvTotalPenjualan;
    private TextView tvTotalBiayaKirim;
    private TextView tvTotalPotonganPenjualan;
    private TextView tvTotalProdukTerjual;
    private TextView tvHppTotal;
    private TextView tvLabaKotorPenjualanProduk,tvLabaBersihPenjualanProduk;
    private RecyclerView rvPenjualan; // Untuk daftar produk terjual

    private RadioGroup rgFilterGrafik;
    private RadioButton rbBulan, rbTahun;
    private Spinner spinnerFilterOption;
    private BarChart chartPenjualan;
    private PieChart pieChartPiutang;
    private TextView tvTotalTerbayar, tvTotalBelumTerbayar;
    private RecyclerView rvTanggalJumlahPenjualan; // Untuk "Tanggal dan Jumlah Penjualan"

    // Adapter untuk RecyclerView list produk terjual
    private ProdukTerjualAdapter produkTerjualAdapter;
    // Adapter untuk RecyclerView bagian bawah (Tanggal & Jumlah Penjualan)
    private TanggalJumlahPenjualanAdapter tanggalJumlahAdapter;

    // ViewModel
    private SaleViewModel saleViewModel;

    private DateHelper dateHelper;
    private StringBuilder dateRange;
    private ActivityResultLauncher<Intent> createFileLauncher;
    double totalChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penjualan, container, false);

        initUI(view);
        setupRecyclerView();
        setupViewModel();
        setupEvents();

//        // Set nilai default untuk selectedDate (misal hari ini)
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        selectedDate.setText("Selected Date: " + sdf.format(new Date()));

        return view;
    }

      private void initUI(View view) {
        // Inisialisasi view
        spinnerFilterDate = view.findViewById(R.id.spinnerFilterDate);
        btnExportCsv = view.findViewById(R.id.btnExportCsv);
        btnExportLaporanCsv = view.findViewById(R.id.btnExportLaporanCsv);
        selectedDate = view.findViewById(R.id.selectedDate);
        tvTotalPenjualan = view.findViewById(R.id.tvTotalPenjualan);
        tvTotalBiayaKirim = view.findViewById(R.id.tvTotalBiayaKirim);
        tvTotalPotonganPenjualan = view.findViewById(R.id.tvTotalPotonganPenjualan);
        tvTotalProdukTerjual = view.findViewById(R.id.tvTotalProdukTerjual);
        tvHppTotal = view.findViewById(R.id.tvHppTotal);
        tvLabaKotorPenjualanProduk = view.findViewById(R.id.tvLabaKotorPenjualanProduk);
          tvLabaBersihPenjualanProduk = view.findViewById(R.id.tvLabaBersihPenjualanProduk);
        rvPenjualan = view.findViewById(R.id.rvPenjualan);

        rgFilterGrafik = view.findViewById(R.id.rgFilterGrafik);
        rbBulan = view.findViewById(R.id.rbBulan);
        rbTahun = view.findViewById(R.id.rbTahun);
        spinnerFilterOption = view.findViewById(R.id.spinnerFilterOption);
        chartPenjualan = view.findViewById(R.id.chartPenjualan);
        pieChartPiutang = view.findViewById(R.id.pieChartPiutang);
        tvTotalTerbayar = view.findViewById(R.id.tvTotalTerbayar);
        tvTotalBelumTerbayar = view.findViewById(R.id.tvTotalBelumTerbayar);
        rvTanggalJumlahPenjualan = view.findViewById(R.id.rvTanggalJumlahPenjualan);
          dateHelper = new DateHelper();
          dateRange = new StringBuilder();
    }

    private void setupRecyclerView() {
        // Setup RecyclerView untuk produk terjual (rvPenjualan) dengan adapter produkTerjualAdapter
        rvPenjualan.setLayoutManager(new LinearLayoutManager(getContext()));
        // Data awal dummy; nanti akan diupdate berdasarkan hasil parsing JSON dari detail sale
        produkTerjualAdapter = new ProdukTerjualAdapter(new ArrayList<>());
        rvPenjualan.setAdapter(produkTerjualAdapter);

        // Setup RecyclerView untuk Tanggal dan Jumlah Penjualan (bagian bawah)
        rvTanggalJumlahPenjualan.setLayoutManager(new LinearLayoutManager(getContext()));
        tanggalJumlahAdapter = new TanggalJumlahPenjualanAdapter(new ArrayList<>());
        rvTanggalJumlahPenjualan.setAdapter(tanggalJumlahAdapter);
    }

    private void setupViewModel() {
        // Inisialisasi ViewModel dan observasi data penjualan
        saleViewModel = new ViewModelProvider(this).get(SaleViewModel.class);
        saleViewModel.getFilteredSalesWithDetails().observe(getViewLifecycleOwner(), salesList -> {

            // Update adapter produk terjual
            List<ProdukTerjual> productSolds = getProdukTerjual(salesList);
            updateSummaryViews(salesList,getNumProductTerjual(productSolds));

            produkTerjualAdapter.updateData(productSolds);
        });

        saleViewModel.getChartData().observe(getViewLifecycleOwner(), chartDataPoints -> {
            updateBarChart(chartDataPoints);
            List<TanggalJumlahItem> aggregatedData = new ArrayList<>();
            for (com.example.kedaiku.viewmodel.ChartDataPoint point : chartDataPoints) {
                aggregatedData.add(new TanggalJumlahItem(point.getLabel(), FormatHelper.formatCurrency(point.getValue())));
            }
            tanggalJumlahAdapter.updateData(aggregatedData);
        });
        // Observer untuk PieChart (menggunakan filteredSalesForChart)
        saleViewModel.getFilteredSalesForChart().observe(getViewLifecycleOwner(), salesListForChart -> {
            updatePieChart(salesListForChart);
        });
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
                saleViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
            } else if (checkedId == R.id.rbTahun) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.filter_options_tahun, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterOption.setAdapter(adapter);
                saleViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
            }
            // Reset flag jika radio group berubah
            isSpinnerChartInitialized = false;
        });

        // Listener untuk spinner filter tanggal (bagian atas)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterDate.setAdapter(adapter);
        spinnerFilterDate.setOnItemSelectedListener(dateHelper.spinnerSelectedListener(
                getContext(), saleViewModel, dateRange, selectedDate, spinnerFilterDate));

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
                        saleViewModel.setChartFilter("Bulan", DateHelper.getStartOfMonth(), DateHelper.getEndOfMonth());
                    } else if (rbTahun.isChecked() && option.equalsIgnoreCase("Tahun ini")) {
                        saleViewModel.setChartFilter("Tahun", DateHelper.getStartOfYear(), DateHelper.getEndOfYear());
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

       btnExportCsv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               exportProductDataToCsv();
           }
       });
    }



    /**
     * Mengupdate summary TextView dengan data agregat dari list SaleWithDetails.
     */
    private void updateSummaryViews(List<SaleWithDetails> salesList,double productSold) {
        double totalPenjualan = 0;
        double totalBiayaKirim = 0;
        double totalPotongan = 0;
        double totalHpp = 0;
        double totalTerbayar = 0;

        for (SaleWithDetails swd : salesList) {

            double realPaid=0; //tanpa kembalian
            if(swd.getSaleTotal()<swd.getSalePaid())
            {
                realPaid=swd.getSaleTotal();
            }
            else {

                realPaid = swd.getSalePaid();
            }

            totalPenjualan += swd.getSaleTotal();
            totalBiayaKirim += swd.getSale().getSale_ship();
            totalPotongan += swd.getSale().getSale_discount();
            totalHpp += swd.getSale().getSale_hpp();
            totalTerbayar += realPaid;
        }
        //int totalProdukTerjual = salesList.size();
        double labaPenjualan = totalPenjualan - totalHpp - totalBiayaKirim;
        double totalBelumTerbayar = totalPenjualan - totalTerbayar;

        tvTotalPenjualan.setText("Total Penjualan: Rp " + totalPenjualan);
        tvTotalBiayaKirim.setText("Total Biaya Kirim: Rp " + totalBiayaKirim);
        tvTotalPotonganPenjualan.setText("Total Potongan Penjualan: Rp " + totalPotongan);
       tvTotalProdukTerjual.setText("Total Produk Terjual: " + productSold);
        tvHppTotal.setText("HPP Total: Rp " + totalHpp);
        tvLabaKotorPenjualanProduk.setText("Laba Kotor Penjualan Produk: Rp " + labaPenjualan);
        tvLabaBersihPenjualanProduk.setText("Laba Bersih Penjualan Produk: Rp " + (labaPenjualan-totalBelumTerbayar));


    }



    /**
     * Meng-update RecyclerView produk terjual (rvPenjualan) dengan mengagregasi data dari
     * detail sale setiap penjualan menggunakan FormatHelper.parseCartItemsFromDetailSale.
     */
    private List<ProdukTerjual> getProdukTerjual(List<SaleWithDetails> salesList) {
        List<CartItem> allCartItems = new ArrayList<>();

        // Kumpulkan semua cart item dari masing-masing sale
        for (SaleWithDetails swd : salesList) {
            // Ambil JSON detail sale (diasumsikan formatnya sesuai dengan fungsi parseCartItemsFromDetailSale)
            String jsonDetail = swd.getDetailSaleItemDetail();
            if (jsonDetail != null && !jsonDetail.isEmpty()) {
                List<CartItem> items = FormatHelper.parseCartItemsFromDetailSale(jsonDetail,swd.getPromo().getDetail());
                allCartItems.addAll(items);
            }
        }

         //Agregasi data berdasarkan productId

        Map<String, CartItem> aggregatedMap = new HashMap<>();
        for (CartItem item : allCartItems) {
            String productName = item.getProductName()+"_"+item.getPriceType(); // pastikan method ini tersedia di CartItem
            if (aggregatedMap.containsKey(productName))
            {
                CartItem existing = aggregatedMap.get(productName);
                assert existing != null;
                //////
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            } else {
                aggregatedMap.put(productName, item);
            }
        }


        // Konversi hasil agregasi ke list ProdukTerjual
        List<ProdukTerjual> produkTerjualList = new ArrayList<>();
        for (CartItem item : aggregatedMap.values()) {
            produkTerjualList.add(new ProdukTerjual(
                    item.getProductName()+"_"+item.getPriceType(),
                    item.getFinalPrice(),
                    item.getQuantity()  // konversi quantity ke integer
            ));
        }

        return produkTerjualList;


    }

    public double getNumProductTerjual(List<ProdukTerjual> solds)
    {
        double totalTerjual=0;

        for(ProdukTerjual item : solds)
        {
            totalTerjual+=item.getQuantitySold();
        }

        return totalTerjual;

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
        chartPenjualan.setData(data);

        // Nonaktifkan label pada sumbu X
        chartPenjualan.getXAxis().setDrawLabels(false);
        chartPenjualan.getAxisLeft().setTextColor(Color.WHITE);
        chartPenjualan.getAxisRight().setTextColor(Color.WHITE);
        chartPenjualan.getDescription().setEnabled(false);
        chartPenjualan.getLegend().setTextColor(Color.WHITE);

        chartPenjualan.invalidate();
    }


    private void updatePieChart(List<SaleWithDetails> salesList) {
        // Ambil opsi spinner untuk chart
        String chartOption = spinnerFilterOption.getSelectedItem() != null ?
                spinnerFilterOption.getSelectedItem().toString() : "";
        double totalPaid = 0;
        double totalUnpaid = 0;

        // Dapatkan parameter filter chart dari ViewModel (bisa berisi custom filter)
        ChartFilterParams chartParams = saleViewModel.getCurrentChartFilterParams();

        if (rbBulan.isChecked() && (chartOption.equalsIgnoreCase("Bulan ini") || chartOption.equalsIgnoreCase("Custom"))) {
            // Gunakan parameter custom jika tersedia, atau fallback ke bulan berjalan
            long monthStart = (chartParams != null && chartParams.startDate != null) ? chartParams.startDate : DateHelper.getStartOfMonth();
            long monthEnd = (chartParams != null && chartParams.endDate != null) ? chartParams.endDate : DateHelper.getEndOfMonth();
            for (SaleWithDetails swd : salesList) {
                long saleDate = swd.getSaleDate();
                if (saleDate >= monthStart && saleDate <= monthEnd) {
                    double saleTotal = swd.getSaleTotal();
                    double salePaid = swd.getSalePaid();
                    if (salePaid > saleTotal) salePaid = saleTotal;
                    totalPaid += salePaid;
                    totalUnpaid += (saleTotal - salePaid);
                }
            }
        } else if (rbTahun.isChecked() && (chartOption.equalsIgnoreCase("Tahun ini") || chartOption.equalsIgnoreCase("Custom"))) {
            // Gunakan parameter custom jika tersedia, atau fallback ke tahun berjalan
            long yearStart = (chartParams != null && chartParams.startDate != null) ? chartParams.startDate : DateHelper.getStartOfYear();
            long yearEnd = (chartParams != null && chartParams.endDate != null) ? chartParams.endDate : DateHelper.getEndOfYear();
            for (SaleWithDetails swd : salesList) {
                long saleDate = swd.getSaleDate();
                if (saleDate >= yearStart && saleDate <= yearEnd) {
                    double saleTotal = swd.getSaleTotal();
                    double salePaid = swd.getSalePaid();
                    if (salePaid > saleTotal) salePaid = saleTotal;
                    totalPaid += salePaid;
                    totalUnpaid += (saleTotal - salePaid);
                }
            }
        } else {
            // Fallback: gunakan semua data pada salesList
            for (SaleWithDetails swd : salesList) {
                double saleTotal = swd.getSaleTotal();
                double salePaid = swd.getSalePaid();
                if (salePaid > saleTotal) salePaid = saleTotal;
                totalPaid += salePaid;
                totalUnpaid += (saleTotal - salePaid);
            }
        }
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalPaid, "Terbayar"));
        entries.add(new PieEntry((float) totalUnpaid, "Belum Terbayar"));
        totalChart=totalPaid+totalUnpaid;
        PieDataSet dataSet = new PieDataSet(entries, "Pembayaran");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        PieData data = new PieData(dataSet);
        pieChartPiutang.setData(data);

        // Update TextView agar nilainya sesuai dengan filter chart
        tvTotalTerbayar.setText("Total Terbayar: Rp " + totalPaid);
        tvTotalBelumTerbayar.setText("Total Belum Terbayar: Rp " + totalUnpaid);
        pieChartPiutang.getLegend().setTextColor(Color.WHITE);

        pieChartPiutang.getDescription().setEnabled(false);


        pieChartPiutang.invalidate();
    }
    // Tampilkan dialog custom untuk filter chart
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
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void exportDataToCsv() {
        // Ambil data dari adapter tanggalJumlahAdapter
        List<TanggalJumlahItem> dataList = tanggalJumlahAdapter.getDataList();
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Definisikan header CSV
        String[] headers = {"Tanggal", "Total Penjualan"};

        // Bangun list baris data CSV dari dataList adapter
        List<String[]> rows = new ArrayList<>();
        for (TanggalJumlahItem item : dataList) {
            String[] row = { item.getTanggal(), item.getJumlah() };
            rows.add(row);
        }

        // Buat judul CSV dengan menyertakan range tanggal (jika ada)
        String judul = "";
        ChartFilterParams chartParams = saleViewModel.getCurrentChartFilterParams();
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
                + "Total Penjualan: Rp. "+totalChart+ "\n"
                + tvTotalTerbayar.getText().toString() + "\n"
                + tvTotalBelumTerbayar.getText().toString() + "\n";

        // Simpan CSV data ke variabel global agar dapat digunakan pada writeCsvDataToUri()
        this.csvData = csvData;

        // Gunakan intent ACTION_CREATE_DOCUMENT untuk membuat file CSV
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "laporan_penjualan.csv");
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


    private void exportProductDataToCsv() {
        // Ambil data dari adapter ProdukTerjualAdapter
        List<ProdukTerjual> dataList = produkTerjualAdapter.getProdukList();
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data produk terjual untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }



        // Bangun judul laporan dengan range tanggal dan summary
        String judul = "Laporan Produk Terjual" + DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(dateHelper.lastFilter)) + "\n\n";
        // Menggabungkan summary dari TextView (pastikan teksnya sudah diformat dengan benar)
        judul += tvTotalPenjualan.getText().toString() + "\n"
                + tvTotalBiayaKirim.getText().toString() + "\n"
                + tvTotalPotonganPenjualan.getText().toString() + "\n"
                + tvTotalProdukTerjual.getText().toString() + "\n"
                + tvHppTotal.getText().toString() + "\n"
                + tvLabaKotorPenjualanProduk.getText().toString() + "\n"
                + tvLabaBersihPenjualanProduk.getText().toString() + "\n\n";

        // Definisikan header untuk tabel data produk terjual
        String[] headers = {"Produk", "Harga", "Jumlah Terjual"};

        // Bangun list baris data CSV
        List<String[]> rows = new ArrayList<>();
        for (ProdukTerjual item : dataList) {
            String[] row = {
                    item.getProductName(),
                    FormatHelper.formatCurrency(item.getProductPrice()),
                    String.valueOf(item.getQuantitySold())
            };
            rows.add(row);
        }

        // Konversi ke CSV menggunakan CsvHelper.convertToCsv()
        String csvData = CsvHelper.convertToCsv(headers, rows, judul);
        this.csvData = csvData;

        // Buat dokumen CSV menggunakan Intent ACTION_CREATE_DOCUMENT
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "laporan_produk_terjual.csv");
        createFileLauncher.launch(intent);
    }

}
