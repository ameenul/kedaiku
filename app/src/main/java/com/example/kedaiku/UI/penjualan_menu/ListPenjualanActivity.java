package com.example.kedaiku.UI.penjualan_menu;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.SaleViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ListPenjualanActivity extends AppCompatActivity {

    private SaleViewModel saleViewModel;
    private SalesAdapter adapter;
    private EditText editTextSearch;
    private Spinner spinnerFilter;
    private TextView textTotalSales, textTotalPaid, textTotalUnpaid;
    private TextView  textViewProfit;
    private TextView textViewTotalHpp;      // Baru
    private TextView textViewNetProfit;
    private TextView textViewSelectedDates;
    private String filter;
    private String lastFilter;
    private String dateRange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_penjualan);

        // Initialize UI elements
        editTextSearch = findViewById(R.id.editTextSearch);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        textTotalSales = findViewById(R.id.textTotalSales);
        textTotalPaid = findViewById(R.id.textTotalPaid);
        textTotalUnpaid = findViewById(R.id.textTotalUnpaid);
        textViewProfit = findViewById(R.id.textViewProfit);
        textViewTotalHpp = findViewById(R.id.textViewTotalHpp);
        textViewNetProfit = findViewById(R.id.textViewNetProfit);
        RecyclerView recyclerViewSales = findViewById(R.id.recyclerViewSales);
        Button buttonExportCsv = findViewById(R.id.buttonExportCsv);
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
                        saleViewModel.deleteSaleTransaction(saleId, new SaleViewModel.OnTransactionCompleteListener() {
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
        
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 filter = parent.getItemAtPosition(position).toString();
//                 Toast.makeText(ListPenjualanActivity.this,filter+ position,Toast.LENGTH_SHORT).show();
                if ("Pilih Tanggal".equals(filter)) {
                    lastFilter = filter;
                    showDateRangePicker();

                }
                else if(!getResources().getStringArray(R.array.filter_options)[0].equals(filter)) {
                    saleViewModel.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter );
                    lastFilter = filter;
                    spinnerFilter.setSelection(0);
                }
                 
                
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up Export CSV button
        buttonExportCsv.setOnClickListener(v -> {
            // Implement export CSV functionality
        });

        // Floating action button to add new sale
        findViewById(R.id.fabAddSale).setOnClickListener(v -> {
            Intent intent = new Intent(ListPenjualanActivity.this, PenjualanKasirActivity.class);
            startActivity(intent);
        });
        
    }

    private void updateUI(List<SaleWithDetails> salesWithDetails) {
        adapter.setSalesWithDetails(salesWithDetails);
        // Update summary views
        double totalSales = 0;
        double totalPaid = 0;
        double totalUnpaid = 0;
        double totalProfit = 0;
        double totalHpp = 0;       // Baru
        double totalNetProfit = 0; // Baru (jika ada

        for (SaleWithDetails saleWithDetails : salesWithDetails) {


            totalSales += saleWithDetails.getSaleTotal();

            double realPaid=0;
            if(saleWithDetails.getSaleTotal()<saleWithDetails.getSalePaid())
            {
                realPaid=saleWithDetails.getSaleTotal();
            }
            else {

                realPaid = saleWithDetails.getSalePaid();
            }

            totalPaid += realPaid;
            totalUnpaid += saleWithDetails.getSaleTotal() - realPaid;
            double saleHpp = saleWithDetails.getSale().getSale_hpp();
            double saleProfit = saleWithDetails.getSaleTotal() - saleHpp;

            totalProfit += saleProfit;


            // Baru
            totalHpp += saleHpp;


        }

        totalNetProfit = totalProfit-totalUnpaid;

        textTotalSales.setText("Total Penjualan: " + FormatHelper.formatCurrency(totalSales) );
        textTotalPaid.setText("Total Terbayar: " + FormatHelper.formatCurrency(totalPaid));
        textTotalUnpaid.setText("Total Belum Terbayar: " + FormatHelper.formatCurrency(totalUnpaid));
        
        textViewProfit.setText("Total Laba Kotor: " + FormatHelper.formatCurrency(totalProfit));
        textViewTotalHpp.setText("Total HPP: " + FormatHelper.formatCurrency(totalHpp));
        textViewNetProfit.setText("Total Laba Bersih: " + FormatHelper.formatCurrency(totalNetProfit));
    }


    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog startDatePicker = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.set(year, month, dayOfMonth);
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    long startDate = startCalendar.getTimeInMillis();

                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            this,
                            (DatePicker endView, int endYear, int endMonth, int endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth);
                                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                                endCalendar.set(Calendar.MINUTE, 59);
                                endCalendar.set(Calendar.SECOND, 59);
                                endCalendar.set(Calendar.MILLISECOND, 999);
                                long endDate = endCalendar.getTimeInMillis();

                                
                                saleViewModel.setDateRangeFilter(startDate, endDate);

                                // Format the selected dates
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
                                String dateRangeText = "Tanggal Terpilih: " +
                                        dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
                                dateRange = dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
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
    
}
