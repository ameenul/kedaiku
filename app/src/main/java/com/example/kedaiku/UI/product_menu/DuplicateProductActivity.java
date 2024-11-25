package com.example.kedaiku.UI.product_menu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.InventoryViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class DuplicateProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductSKU,
            editTextProductPrimaryPrice, editTextProductPrice, editTextProductQty, editTextProductUnit;
    private ProductViewModel productViewModel;
    private InventoryViewModel inventoryViewModel;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplicate_product);
        // Inisialisasi ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

//
//        // Inisialisasi Views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductSKU = findViewById(R.id.editTextProductSKU);
        editTextProductPrimaryPrice = findViewById(R.id.editTextProductPrimaryPrice);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductQty = findViewById(R.id.editTextProductQty);
        editTextProductUnit = findViewById(R.id.editTextProductUnit);
        Button buttonAddProduct = findViewById(R.id.buttonAddProduct);
        TextView textViewPrimaryPricePreview = findViewById(R.id.textViewPrimaryPricePreview);
        TextView textViewPricePreview = findViewById(R.id.textViewPricePreview);

        // Ambil data produk dari intent
        long productId = getIntent().getLongExtra("product_id",-1);
        if (productId == -1) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        // Dapatkan data produk berdasarkan ID
        productViewModel.getProductById(productId).observe(this, fetchedProduct -> {
            if (fetchedProduct != null) {
                product = fetchedProduct;
                populateFields(product);
            }
        });
//
//        // Tombol Update Produk
        buttonAddProduct.setOnClickListener(v -> {
            if (product != null) {
                addProduct();
            }
        });



        // Tambahkan TextWatcher untuk preview harga pokok
        editTextProductPrimaryPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(value);
                        textViewPrimaryPricePreview.setText(formatToRupiah(amount));
                    } catch (NumberFormatException e) {
                        textViewPrimaryPricePreview.setText("Rp 0");
                    }
                } else {
                    textViewPrimaryPricePreview.setText("Rp 0");
                }
            }
        });

        // Tambahkan TextWatcher untuk preview harga jual
        editTextProductPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(value);
                        textViewPricePreview.setText(formatToRupiah(amount));
                    } catch (NumberFormatException e) {
                        textViewPricePreview.setText("Rp 0");
                    }
                } else {
                    textViewPricePreview.setText("Rp 0");
                }
            }
        });
    }

    // Fungsi untuk memformat angka ke Rupiah
    private String formatToRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount);
    }


    private void populateFields(Product product) {
        editTextProductName.setText(product.getProduct_name());
        editTextProductDescription.setText(product.getProduct_description());
        editTextProductSKU.setText(product.getProduct_sku());
        editTextProductPrimaryPrice.setText(String.valueOf(product.getProduct_primary_price()));
        editTextProductPrice.setText(String.valueOf(product.getProduct_price()));
        editTextProductQty.setText(String.valueOf(product.getProduct_qty()));
        editTextProductUnit.setText(product.getProduct_unit());
    }

    private void addProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String productDescription = editTextProductDescription.getText().toString().trim();
        String productSKU = editTextProductSKU.getText().toString().trim();
        String primaryPriceString = editTextProductPrimaryPrice.getText().toString().trim();
        String priceString = editTextProductPrice.getText().toString().trim();
        String qtyString = editTextProductQty.getText().toString().trim();
        String productUnit = editTextProductUnit.getText().toString().trim();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productDescription)
                || TextUtils.isEmpty(productSKU) || TextUtils.isEmpty(primaryPriceString)
                || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(qtyString)
                || TextUtils.isEmpty(productUnit)) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double primaryPrice = Double.parseDouble(primaryPriceString);
        double price = Double.parseDouble(priceString);
        double qty = Double.parseDouble(qtyString);

        Product product = new Product(
                productName,
                productDescription,
                productSKU,
                price,
                primaryPrice,
                productUnit,
                qty
        );

        // Create initial Inventory record
        Inventory inventory = new Inventory(
                getCurrentDate(),
                (int) product.getId(), // Will be set after insertion
                "Initial Stock",
                qty,  // stock_in
                0,    // stock_out
                qty   // stock_balance
        );

        // Insert product and inventory in one transaction
        productViewModel.insertProductWithInventory(product, inventory);

        Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        finish();
    }

    private long getCurrentDate() {
        return System.currentTimeMillis();
//        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }


}
