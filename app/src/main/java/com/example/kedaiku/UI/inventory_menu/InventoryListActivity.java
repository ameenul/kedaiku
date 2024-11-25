package com.example.kedaiku.UI.inventory_menu;

import android.app.DatePickerDialog;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.InventoryViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

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
                Log.d("masuk sini", "onItemSelected: ");
                filter = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(filter)) {
                    showDateRangePicker();

                } else if(!getResources().getStringArray(R.array.filter_options)[0].equals(filter)) {
                    inventoryViewModel.setFilter(filter);
                    textViewSelectedDates.setText("Tanggal Terpilih: " + filter );
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
                    long startDate = startCalendar.getTimeInMillis();

                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            this,
                            (DatePicker endView, int endYear, int endMonth, int endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth);
                                long endDate = endCalendar.getTimeInMillis();

                                inventoryViewModel.setDateRangeFilter(startDate, endDate);

                                // Format the selected dates
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                String dateRangeText = "Tanggal Terpilih: " +
                                        dateFormat.format(startCalendar.getTime()) + " - " +
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
}



