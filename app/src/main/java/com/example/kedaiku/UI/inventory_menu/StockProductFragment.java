package com.example.kedaiku.UI.inventory_menu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
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
    private ProductViewModel productViewModel;
    private Spinner spinnerFilter;
    private TextView textViewSelectedDates;
    private EditText editTextSearch;
    private FloatingActionButton fabExportCsv;

    private List<ProductInventory> fullProductInventoryList = new ArrayList<>();

    private ExecutorService executorService;
    private Future<?> searchTask;

    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;

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

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        executorService = Executors.newSingleThreadExecutor();

        setupSpinner();
        setupSearch();
        observeFilteredProductInventory();

        // Set filter awal
        productViewModel.setFilter("Semua Waktu");
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

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position,long id) {
                String filter = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(filter)) {
                    showDateRangePicker();
                } else {
                    productViewModel.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter + " (Klik Tiap Item Untuk Detail)");
                    // spinnerFilter.setSelection(0); // Remove or comment this line if not needed
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productViewModel.setFilter("Semua Waktu");
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
                filterAndDisplayProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak perlu
            }
        });
    }

    private void filterAndDisplayProducts(String query) {
        // Cancel any ongoing search task
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(true);
        }

        // Start a new search task
        searchTask = executorService.submit(() -> {
            List<ProductInventory> filteredList = new ArrayList<>();

            if (fullProductInventoryList == null) {
                fullProductInventoryList = new ArrayList<>();
            }

            if (query == null || query.isEmpty()) {
                filteredList = new ArrayList<>(fullProductInventoryList);
            } else {
                String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
                for (ProductInventory item : fullProductInventoryList) {
                    if (Thread.currentThread().isInterrupted()) {
                        // Task was cancelled
                        return;
                    }
                    String productName = item.getProductName();
                    if (productName != null && productName.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                        filteredList.add(item);
                    }
                }
            }

            // Update the adapter on the main thread
            List<ProductInventory> finalFilteredList = filteredList;
            requireActivity().runOnUiThread(() -> {
                adapter.submitList(finalFilteredList);
            });
        });
    }

    private void observeFilteredProductInventory() {
        productViewModel.getFilteredProductInventory().observe(getViewLifecycleOwner(), inventoryItems -> {
            if (inventoryItems != null) {
                fullProductInventoryList = inventoryItems; // Save full data
                filterAndDisplayProducts(editTextSearch.getText().toString()); // Apply current search query
                Log.d("StockProductFragment", "Products loaded: " + inventoryItems.size());
            } else {
                fullProductInventoryList = new ArrayList<>();
                filterAndDisplayProducts(editTextSearch.getText().toString());
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
                                textViewSelectedDates.setText(dateRangeText);
                                spinnerFilter.setSelection(0);

                                // Set custom date range in ViewModel
                                productViewModel.setCustomDateRange(startDate, endDate);
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
