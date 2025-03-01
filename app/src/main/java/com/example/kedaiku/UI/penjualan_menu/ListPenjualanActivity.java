package com.example.kedaiku.UI.penjualan_menu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;

import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.repository.OnTransactionCompleteListener;
import com.example.kedaiku.viewmodel.SaleViewModel;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListPenjualanActivity extends AppCompatActivity {

    private SaleViewModel saleViewModel;
    private SalesAdapter adapter;
    private EditText editTextSearch;
    private Spinner spinnerFilter;
    private TextView textTotalSales, textTotalPaid, textTotalUnpaid, textTotalOngkir;
    private TextView  textViewProfit;
    private TextView textViewTotalHpp;      // Baru
    private TextView textViewNetProfit;
    private TextView textViewSelectedDates;
    private Button buttonExportCsv ;
    StringBuilder dateRange;
    private DateHelper dateHelper;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_penjualan);

        // Initialize UI elements
        dateHelper = new DateHelper();
        dateRange = new StringBuilder();
        editTextSearch = findViewById(R.id.editTextSearch);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        textTotalSales = findViewById(R.id.textTotalSales);
        textTotalPaid = findViewById(R.id.textTotalPaid);
        textTotalUnpaid = findViewById(R.id.textTotalUnpaid);
        textViewProfit = findViewById(R.id.textViewProfit);
        textViewTotalHpp = findViewById(R.id.textViewTotalHpp);
        textViewNetProfit = findViewById(R.id.textViewNetProfit);
        textTotalOngkir = findViewById(R.id.textTotalShipping);
        RecyclerView recyclerViewSales = findViewById(R.id.recyclerViewSales);
        buttonExportCsv = findViewById(R.id.buttonExportCsv);
        textViewSelectedDates = findViewById(R.id.textViewSelectedDates);

        // Initialize RecyclerView and Adapter
        recyclerViewSales.setLayoutManager(new LinearLayoutManager(this));


        // Di ListPenjualanActivity.java, saat membuat adapter:
        adapter = new SalesAdapter(
                this,
                new ArrayList<>(),
                new SalesAdapter.OnItemClickListener() {
                    @Override
                    public void onEditClicked(SaleWithDetails saleWithDetails) {
                        long saleId = saleWithDetails.getSaleId();
                        long promoId = saleWithDetails.getPromo().getId();
                        long saleDetailId = saleWithDetails.getDetailSale().getId();
                        // Arahkan ke Activity "PenjualanKasirActivity", kirim sale_id
                        Intent intent = new Intent(ListPenjualanActivity.this, PenjualanKasirActivity.class);
                        intent.putExtra("sale_id", saleId);
                        // Agar kita tahu mode “EDIT”, bisa tambahkan extra:
                        intent.putExtra("edit_mode", true);
                        intent.putExtra("edit_init", true);
                        intent.putExtra("detailSale_id", saleDetailId);
                        intent.putExtra("promo_id", promoId);

                        ListPenjualanActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onDeleteClicked(SaleWithDetails saleWithDetails) {
                        // Panggil fungsi di ViewModel untuk menghapus data penjualan
                        long saleId = saleWithDetails.getSaleId();
                        saleViewModel.deleteSaleTransaction(saleId, new OnTransactionCompleteListener() {
                            @Override
                            public void onSuccess(boolean status) {
                                // status = true -> sukses
                                Toast.makeText(ListPenjualanActivity.this,
                                        "Hapus penjualan berhasil!",
                                        Toast.LENGTH_SHORT).show();
                                // Mungkin Anda ingin memanggil saleViewModel.refreshData() atau semacamnya
                            }

                            @Override
                            public void onFailure(boolean status) {
                                // status = false -> gagal
                                Toast.makeText(ListPenjualanActivity.this,
                                        "Hapus penjualan gagal!",
                                        Toast.LENGTH_SHORT).show();
                            }


                        });
                    }

                    @Override
                    public void onItemClicked(SaleWithDetails saleWithDetails) {
                        // Implementasi untuk membuka PenjualanDetailActivity
                        long saleId = saleWithDetails.getSaleId();
                        Intent intent = new Intent(ListPenjualanActivity.this, DetailPenjualanActivity.class);
                        intent.putExtra("sale_id", saleId);
                        ListPenjualanActivity.this.startActivity(intent);
                    }
                }
        );

        recyclerViewSales.setAdapter(adapter);


             // Initialize ViewModel
        saleViewModel = new ViewModelProvider(this).get(SaleViewModel.class);

        // Observe the LiveData from the ViewModel
        saleViewModel.getFilteredSalesWithDetails().observe(this, salesWithDetails -> {
            updateUI(salesWithDetails);
        });

        // Set up search filter
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saleViewModel.setTransactionNameFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up filter spinner
        
        spinnerFilter.setOnItemSelectedListener(dateHelper.spinnerSelectedListener(ListPenjualanActivity.this

        , saleViewModel, dateRange, textViewSelectedDates, spinnerFilter));



        // Floating action button to add new sale
        findViewById(R.id.fabAddSale).setOnClickListener(v -> {
            Intent intent = new Intent(ListPenjualanActivity.this, PenjualanKasirActivity.class);
            startActivity(intent);
        });

        // Inisialisasi ActivityResultLauncher
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

        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());



    }

    private void updateUI(List<SaleWithDetails> salesWithDetails) {
        adapter.setSalesWithDetails(salesWithDetails);
        // Update summary views
        double totalSales = 0;
        double totalPaid = 0;
        double totalUnpaid = 0;
        double totalProfit = 0;
        double totalHpp = 0;
        double totalNetProfit = 0;
        double totalShippingCost = 0;

        for (SaleWithDetails saleWithDetails : salesWithDetails) {




            double realPaid=0; //tanpa kembalian
            if(saleWithDetails.getSaleTotal()<saleWithDetails.getSalePaid())
            {
                realPaid=saleWithDetails.getSaleTotal();
            }
            else {

                realPaid = saleWithDetails.getSalePaid();
            }

            totalSales += saleWithDetails.getSaleTotal();
            totalPaid += realPaid;
            totalUnpaid += saleWithDetails.getSaleTotal() - realPaid;
            totalShippingCost += saleWithDetails.getSale().getSale_ship();

            double saleHpp = saleWithDetails.getSale().getSale_hpp();
            double saleProfit = saleWithDetails.getSaleTotal() - saleHpp - saleWithDetails.getSale().getSale_ship();
            totalProfit += saleProfit;


            // Baru
            totalHpp += saleHpp;


        }

        totalNetProfit = totalProfit-totalUnpaid;

        textTotalSales.setText("Total Penjualan: " + FormatHelper.formatCurrency(totalSales) );
        textTotalPaid.setText("Total Terbayar: " + FormatHelper.formatCurrency(totalPaid));
        textTotalUnpaid.setText("Total Belum Terbayar: " + FormatHelper.formatCurrency(totalUnpaid));
        textTotalOngkir.setText("Total Ongkir : "+totalShippingCost);
        
        textViewProfit.setText("Total Laba Kotor: " + FormatHelper.formatCurrency(totalProfit));
        textViewTotalHpp.setText("Total HPP: " + FormatHelper.formatCurrency(totalHpp));
        textViewNetProfit.setText("Total Laba Bersih: " + FormatHelper.formatCurrency(totalNetProfit));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Lepas listener untuk spinnerFilter
        spinnerFilter.setOnItemSelectedListener(null);


    }

    private void exportDataToCsv() {
        List<SaleWithDetails> currentSales = adapter.getSalesList();
        if (currentSales == null || currentSales.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tentukan header dan baris data untuk CSV
        String[] headers = {"Sale ID", "Transaction Name", "Date", "Total", "Paid", "Payment Type", "Customer Name"};
        List<String[]> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (SaleWithDetails sale : currentSales) {
            String saleId = String.valueOf(sale.getSaleId());
            String transactionName = sale.getSaleTransactionName();
            String dateStr = sdf.format(new Date(sale.getSaleDate()));
            String total = String.valueOf(sale.getSaleTotal());
            String paid = String.valueOf(sale.getSalePaid());
            String paymentType = sale.getSale().getSale_payment_type() == 1 ? "Cash" : "Piutang";
            String customerName = sale.getCustomerName();

            String[] row = { saleId, transactionName, dateStr, total, paid, paymentType, customerName };
            rows.add(row);
        }

        String judul = "Export Data Penjualan";
        if(!dateHelper.lastFilter.equals("Pilih Tanggal")) {
            judul = ("List Penjualan_search = " +editTextSearch.getText()+" "+ DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(dateHelper.lastFilter)) + " \n");

        }
        else{
            judul = ("List Penjualan_search = " +editTextSearch.getText()+" "+ dateRange.toString()+ " \n");

        }
        csvData = CsvHelper.convertToCsv(headers, rows, judul);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "penjualan_export.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




}
