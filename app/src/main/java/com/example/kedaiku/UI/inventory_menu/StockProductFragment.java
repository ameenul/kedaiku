package com.example.kedaiku.UI.inventory_menu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.viewmodel.ProductInventoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StockProductFragment extends Fragment {

    private StockProductAdapter adapter;
    private ProductInventoryViewModel productInventoryViewModel;
    private Spinner spinnerFilter;
    private TextView textViewSelectedDates;
    private EditText editTextSearch;
    private FloatingActionButton fabExportCsv;

    private List<ProductInventory> fullProductInventoryList = new ArrayList<>();

    private ExecutorService executorService;
    private Future<?> searchTask;

    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;
    String currentFilterDate;
    String currentFilterSearch;
    private String dateRange;
    boolean isFirst=true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_stock_product, container, false);

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
                });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewStockProduct);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        textViewSelectedDates = view.findViewById(R.id.textViewSelectedDates);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        fabExportCsv = view.findViewById(R.id.fabExportCsv);

        adapter = new StockProductAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        productInventoryViewModel = new ViewModelProvider(this).get(ProductInventoryViewModel.class);

        executorService = Executors.newSingleThreadExecutor();

        setupSpinner();
        setupSearch();
        observeFilteredProductInventory();

        // Set filter awal
        currentFilterDate = "Semua Waktu";
        currentFilterSearch = "";
        productInventoryViewModel.setFilter(currentFilterDate,currentFilterSearch);
        textViewSelectedDates.setText("Tanggal Terpilih: Semua Waktu (Klik Tiap Item Untuk Detail)");

        // Set up FAB click listener
        fabExportCsv.setOnClickListener(v -> exportDataToCsv());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void setupSpinner() {
        // Set up filter spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFilter.setAdapter(adapter);
        isFirst=true;

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position,long id)
            {

//                Toast.makeText(getContext(), "Filter: " + position, Toast.LENGTH_SHORT).show();
                currentFilterDate = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(currentFilterDate)) {
                    showDateRangePicker();
                }
                else if(position==0) {
                    if(isFirst)
                    {
//                        Toast.makeText(getContext(), "masuk: " + position, Toast.LENGTH_SHORT).show();
                        isFirst=false;
                        currentFilterDate = "Semua Waktu";
                        productInventoryViewModel.setFilter(currentFilterDate,currentFilterSearch);
                        textViewSelectedDates.setText("Tanggal Terpilih: " + currentFilterDate + " (Klik Tiap Item Untuk Detail)");
                    }

                }
                else {
                    productInventoryViewModel.setFilter(currentFilterDate,currentFilterSearch);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + currentFilterDate + " (Klik Tiap Item Untuk Detail)");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "nothing", Toast.LENGTH_SHORT).show();
                currentFilterDate = "Semua Waktu";
                currentFilterSearch = "";
                productInventoryViewModel.setFilter(currentFilterDate,currentFilterSearch);
                textViewSelectedDates.setText("Tanggal Terpilih: Semua Waktu (Klik Tiap Item Untuk Detail)");
            }
        });
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {
                // Tidak perlu
            }

            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                currentFilterSearch=s.toString();
                productInventoryViewModel.setFilter(currentFilterDate,currentFilterSearch);
               // filterAndDisplayProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak perlu
            }
        });
    }

    private void observeFilteredProductInventory() {
        productInventoryViewModel.getFilteredProductInventory().observe(getViewLifecycleOwner(), inventoryItems -> {
            if (inventoryItems != null) {
                fullProductInventoryList = inventoryItems; // Save full data
//                filterAndDisplayProducts(editTextSearch.getText().toString()); // Apply current search query
                Log.d("StockProductFragment", "Products loaded: " + inventoryItems.size());
                adapter.submitList(fullProductInventoryList);
            } else {
                fullProductInventoryList = new ArrayList<>();
                adapter.submitList(fullProductInventoryList);
              //  filterAndDisplayProducts(editTextSearch.getText().toString());
                Log.d("StockProductFragment", "No products available.");
            }
        });
    }

    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();

        // DatePickerDialog for Start Date
        DatePickerDialog startDatePicker = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    long startDate = startCalendar.getTimeInMillis();

                    // DatePickerDialog for End Date
                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            getContext(),
                            (DatePicker endView, int endYear, int endMonth, int endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth, 23, 59, 59);
                                endCalendar.set(Calendar.MILLISECOND, 999);
                                long endDate = endCalendar.getTimeInMillis();

                                Log.d("StockProductFragment", "Start Date: " + startDate + ", End Date: " + endDate);

                                // Format selected dates
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                String dateRangeText = "Tanggal Terpilih: " +
                                        dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime()) + "  (Klik Tiap Item Untuk Detail)";

                                //buat csv
                                dateRange = dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
                                //


                                textViewSelectedDates.setText(dateRangeText);
                                spinnerFilter.setSelection(0);

                                // Set custom date range in ViewModel
                                productInventoryViewModel.setCustomDateRange(startDate, endDate,currentFilterSearch);
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

                    endDatePicker.setTitle("Pilih Tanggal Akhir");
                    endDatePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        startDatePicker.setTitle("Pilih Tanggal Mulai");
        startDatePicker.show();
    }

    private void exportDataToCsv() {
        List<ProductInventory> dataToExport = adapter.getProductInventoryList();
        if (dataToExport == null || dataToExport.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the CSV data
        StringBuilder data = new StringBuilder();

        // Prepare the CSV data

        if(!currentFilterDate.equals("Pilih Tanggal")) {
            data.append("Inventory_"+ DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(currentFilterDate))+" : "+currentFilterSearch+ " \n");

        }
        else{
            data.append("Inventory_"+dateRange +" : "+currentFilterSearch+ " \n");

        }
        data.append("\n");



        data.append("Produk_ID,Produk,Stok Masuk,Stok Keluar,Stok Akhir\n");

        for (ProductInventory item : dataToExport) {
            data.append(item.getProductId()).append(",");
            data.append(item.getProductName()).append(",");
            data.append(item.getStockIn()).append(",");
            data.append(item.getStockOut()).append(",");
            data.append(item.getStockBalance()).append("\n");
        }

        // Store the CSV data in a variable
        csvData = data.toString();

        // Create an intent to let the user choose where to save the CSV file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "ProductInventory.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

