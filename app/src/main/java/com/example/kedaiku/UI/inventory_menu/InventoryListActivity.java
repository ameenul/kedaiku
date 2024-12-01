package com.example.kedaiku.UI.inventory_menu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.viewmodel.InventoryViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InventoryListActivity extends AppCompatActivity {

    private InventoryViewModel inventoryViewModel;
    private ProductViewModel productViewModel;
    private InventoryListAdapter adapter;
    private Spinner spinnerFilter;
    private TextView textViewSelectedDates;
    private long stockProductId;

    private Product currentProduct;
    String filter;

    private Button buttonExportCsv;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;
    private String dateRange;
    private String lastFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        buttonExportCsv = findViewById(R.id.buttonExportCsv);


        TextView productName = findViewById(R.id.textViewProductName);
        TextView productSKU = findViewById(R.id.textViewProductSKU);
        TextView primaryPrice = findViewById(R.id.textViewPrimaryPrice);
        TextView productPrice = findViewById(R.id.textViewProductPrice);
        TextView productQty = findViewById(R.id.textViewProductQty);
        TextView productUnit = findViewById(R.id.textViewProductUnit);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        textViewSelectedDates = findViewById(R.id.textViewSelectedDates);
        FloatingActionButton fabAddStock = findViewById(R.id.fabAddStock);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewInventory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryListAdapter();
        recyclerView.setAdapter(adapter);

        stockProductId = getIntent().getLongExtra("product_id", -1);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);


        // Set stockProductId ke ViewModel
        inventoryViewModel.setStockProductId(stockProductId);

        // Observe product details
        productViewModel.getProductById(stockProductId).observe(this, product -> {
            if (product != null) {
                currentProduct = product; // Save product for later updates
                productName.setText("Nama Produk: " + product.getProduct_name());
                productSKU.setText("SKU: " + product.getProduct_sku());
                primaryPrice.setText("Harga Pokok: " + product.getProduct_primary_price());
                productPrice.setText("Harga Jual: " + product.getProduct_price());
                productQty.setText("Stok: " + product.getProduct_qty());
                productUnit.setText("Satuan: " + product.getProduct_unit());
            }
        });

        // Observe filtered inventory changes
        inventoryViewModel.getFilteredInventories().observe(this, inventoryList -> {
            adapter.submitList(inventoryList);
        });



                setupSpinner();

        fabAddStock.setOnClickListener(v -> showStockDialog(stockProductId));


        // Initialize ActivityResultLauncher for file creation
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

        // Set up Export CSV button click listener
        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());
        lastFilter="Semua Waktu";

    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);


        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               // Log.d("masuk sini", "onItemSelected: ");
                filter = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(filter)) {
                    lastFilter = filter;
                    showDateRangePicker();

                } else if(!getResources().getStringArray(R.array.filter_options)[0].equals(filter)) {
                    inventoryViewModel.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter );
                    lastFilter = filter;
                    spinnerFilter.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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

                                inventoryViewModel.setDateRangeFilter(startDate, endDate);

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

    private void showStockDialog(long stockProductId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_inventory, null);
        builder.setView(dialogView);

        EditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        RadioGroup radioGroupTransaction = dialogView.findViewById(R.id.radioGroupTransaction);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String description = editTextDescription.getText().toString();
            String quantityText = editTextQuantity.getText().toString();

            if (quantityText.isEmpty()) {
                Toast.makeText(this, "Jumlah tidak boleh kosong.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioGroupTransaction.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Pilih jenis transaksi.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isStockIn = radioGroupTransaction.getCheckedRadioButtonId() == R.id.radioStockIn;
            double quantity ;
            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Jumlah harus berupa angka.", Toast.LENGTH_SHORT).show();
                return;
            }
            double oldQty = currentProduct.getProduct_qty();
            double newQty = isStockIn ? oldQty + quantity : oldQty - quantity;

            if (newQty < 0) {
                Toast.makeText(this, "Stok tidak bisa negatif.", Toast.LENGTH_SHORT).show();
                return;
            }

            currentProduct.setProduct_qty(newQty);
            productViewModel.updateProductWithInventory(currentProduct, oldQty, description);


//            inventoryViewModel.setFilter(filter);
//            textViewSelectedDates.setText("Tanggal Terpilih: " + filter );


            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void exportDataToCsv() {
        List<Inventory> dataToExport = adapter.getInventoryList();
        if (dataToExport == null || dataToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the CSV data
        StringBuilder data = new StringBuilder();

        if(!lastFilter.equals("Pilih Tanggal")) {
            data.append("Inventory_" + currentProduct.getProduct_name() +" "+ DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(lastFilter)) + " \n");

        }
        else{
            data.append("Inventory_" + currentProduct.getProduct_name()+" "+dateRange);

        }
            data.append("\n");

        data.append("Tanggal,Deskripsi,Masuk,Keluar,Saldo\n");

        for (Inventory item : dataToExport) {
            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
            String date = dateFormat.format(item.getStock_date());

            data.append(date).append(",");
            data.append(item.getStock_note()).append(",");
            data.append(item.getStock_in()).append(",");
            data.append(item.getStock_out()).append(",");
            data.append(item.getStock_balance()).append("\n");
        }

        // Store the CSV data in a variable
        csvData = data.toString();

        // Create an intent to let the user choose where to save the CSV file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Inventory_" + currentProduct.getProduct_name() + ".csv");
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

    private long[] calculateDateRange(String filterName) {
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
    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getStartOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getStartOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay() {

        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        day.set(Calendar.MILLISECOND, 999);
        return day.getTimeInMillis();
    }

}



