package com.example.kedaiku.UI.product_menu;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.InventoryViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductSKU,
            editTextProductPrimaryPrice, editTextProductPrice, editTextProductQty, editTextProductUnit;
    private ProductViewModel productViewModel;
  //  private InventoryViewModel inventoryViewModel;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

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

//        productViewModel.update(product);
//
//        // Jika stok berubah, catat di tabel inventory
//        if (newQty != oldQty) {
//            double stockChange = newQty - oldQty;
//            String stockNote = "Update jumlah barang dari " + oldQty + " ke " + newQty;
//            addInventoryRecord(product.getId(), stockChange, stockNote);
//        }
//
//        Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
        finish();
    }

//    private void addInventoryRecord(long productId, double stockChange, String note) {
//       // long stockDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        long stockDate = System.currentTimeMillis();
//        double stockIn = stockChange > 0 ? stockChange : 0;
//        double stockOut = stockChange < 0 ? Math.abs(stockChange) : 0;
//        double stockBalance = product.getProduct_qty();
//
//        Inventory inventory = new Inventory(
//                 // Auto-generated ID
//                stockDate,
//                productId,
//                note,
//                stockIn,
//                stockOut,
//                stockBalance
//        );
//
//        inventoryViewModel.insert(inventory);
//    }
}
