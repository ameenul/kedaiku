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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductSKU,
            editTextProductPrimaryPrice, editTextProductPrice, editTextProductQty, editTextProductUnit;
    private ProductViewModel productViewModel;
  //  private InventoryViewModel inventoryViewModel;
    private Product product;
    private NumberFormat currencyFormat;
    private TextView textViewFormattedPrimaryPrice, textViewFormattedPrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        // Initialize currency format for Rupiah
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Inisialisasi ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
       // inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Inisialisasi Views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductSKU = findViewById(R.id.editTextProductSKU);
        editTextProductPrimaryPrice = findViewById(R.id.editTextProductPrimaryPrice);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductQty = findViewById(R.id.editTextProductQty);
        editTextProductUnit = findViewById(R.id.editTextProductUnit);
        textViewFormattedPrimaryPrice = findViewById(R.id.textViewPrimaryPricePreview);
        textViewFormattedPrice = findViewById(R.id.textViewPricePreview);

        Button buttonUpdateProduct = findViewById(R.id.buttonUpdateProduct);

        // Ambil data produk dari intent
        long productId = getIntent().getLongExtra("product_id",-1);
        if (productId == -1) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Dapatkan data produk berdasarkan ID
        productViewModel.getProductById(productId).observe(this, fetchedProduct -> {
            if (fetchedProduct != null) {
                product = fetchedProduct;
                populateFields(product);
            }
        });

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

        // Tombol Update Produk
        buttonUpdateProduct.setOnClickListener(v -> {
            if (product != null) {
                updateProduct();
            }
        });
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

    private void updateProduct() {
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
        double newQty = Double.parseDouble(qtyString);
        double oldQty = product.getProduct_qty();

        // Update produk
        product.setProduct_name(productName);
        product.setProduct_description(productDescription);
        product.setProduct_sku(productSKU);
        product.setProduct_primary_price(primaryPrice);
        product.setProduct_price(price);
        product.setProduct_qty(newQty);
        product.setProduct_unit(productUnit);


        productViewModel.updateProductWithInventory(product,oldQty);


        finish();
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

}
