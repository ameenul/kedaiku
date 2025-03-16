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
import com.example.kedaiku.viewmodel.ProductViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductSKU,
            editTextProductPrimaryPrice, editTextProductPrice, editTextProductQty, editTextProductUnit;
    private TextView textViewFormattedPrimaryPrice, textViewFormattedPrice;
    private ProductViewModel productViewModel;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize currency format for Rupiah
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize Views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductSKU = findViewById(R.id.editTextProductSKU);
        editTextProductPrimaryPrice = findViewById(R.id.editTextProductPrimaryPrice);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductQty = findViewById(R.id.editTextProductQty);
        editTextProductUnit = findViewById(R.id.editTextProductUnit);

        textViewFormattedPrimaryPrice = findViewById(R.id.textViewFormattedPrimaryPrice);
        textViewFormattedPrice = findViewById(R.id.textViewFormattedPrice);

        Button buttonSaveProduct = findViewById(R.id.buttonSaveProduct);

        // TextWatcher for Harga Pokok
        editTextProductPrimaryPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateFormattedPrice(s.toString(), textViewFormattedPrimaryPrice);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // TextWatcher for Harga Jual
        editTextProductPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateFormattedPrice(s.toString(), textViewFormattedPrice);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Button Simpan Produk
        buttonSaveProduct.setOnClickListener(v -> {
            String productName = editTextProductName.getText().toString().trim();
            String productDescription = editTextProductDescription.getText().toString().trim();
            String productSKU = editTextProductSKU.getText().toString().trim();
            String primaryPriceString = editTextProductPrimaryPrice.getText().toString().trim();
            String priceString = editTextProductPrice.getText().toString().trim();
            String qtyString = editTextProductQty.getText().toString().trim();
            String productUnit = editTextProductUnit.getText().toString().trim();

            if(TextUtils.isEmpty(productSKU))
            {
                productSKU="-";
            }
            // Validasi input
            if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productDescription)
                     || TextUtils.isEmpty(primaryPriceString)
                    || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(qtyString)
                    || TextUtils.isEmpty(productUnit)) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            double primaryPrice = Double.parseDouble(primaryPriceString);
            double price = Double.parseDouble(priceString);
            double qty = Double.parseDouble(qtyString);

            // Create Product object
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
            finish(); // Kembali ke halaman sebelumnya
        });
    }

    private void updateFormattedPrice(String priceString, TextView textView) {
        if (!priceString.isEmpty()) {
            try {
                double price = Double.parseDouble(priceString);
                String formattedPrice = currencyFormat.format(price);
                textView.setText(formattedPrice);
            } catch (NumberFormatException e) {
                textView.setText("Rp0");
            }
        } else {
            textView.setText("Rp0");
        }
    }

    private long getCurrentDate() {
        return System.currentTimeMillis();
//        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }
}
