package com.example.kedaiku.UI.inventory_menu;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.InventoryViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddPurchaseActivity extends AppCompatActivity {

    private Spinner spinnerProduct;
    private EditText editTextPurchaseQuantity;
    private EditText editTextPurchaseNote;
    private Button buttonSavePurchase;

    private ProductViewModel productViewModel;
    private InventoryViewModel inventoryViewModel;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        spinnerProduct = findViewById(R.id.spinnerProduct);
        editTextPurchaseQuantity = findViewById(R.id.editTextPurchaseQuantity);
        editTextPurchaseNote = findViewById(R.id.editTextPurchaseNote);
        buttonSavePurchase = findViewById(R.id.buttonSavePurchase);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Memuat daftar produk ke Spinner
        productViewModel.getAllProducts().observe(this, products -> {
            productList = products;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getProductNames(products));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProduct.setAdapter(adapter);
        });

        // Tombol Simpan
        buttonSavePurchase.setOnClickListener(v -> savePurchase());
    }

    private void savePurchase() {
        if (productList == null || productList.isEmpty()) {
            Toast.makeText(this, "Tidak ada produk untuk dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil data dari Spinner dan EditText
        int selectedProductIndex = spinnerProduct.getSelectedItemPosition();
        Product selectedProduct = productList.get(selectedProductIndex);
        String quantityString = editTextPurchaseQuantity.getText().toString().trim();
        String note = editTextPurchaseNote.getText().toString().trim();

        if (quantityString.isEmpty()) {
            Toast.makeText(this, "Jumlah pembelian harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity = Double.parseDouble(quantityString);

        // Buat catatan pembelian di tabel Inventory
        String purchaseNote = note.isEmpty() ? "Pembelian produk" : note;
        Inventory inventory = new Inventory(
                System.currentTimeMillis(),
                selectedProduct.getId(),
                purchaseNote,
                quantity, // stock_in
                0, // stock_out
                selectedProduct.getProduct_qty() + quantity // stock_balance
        );

        // Simpan pembelian dan perbarui stok
        selectedProduct.setProduct_qty(selectedProduct.getProduct_qty() + quantity);
        productViewModel.updateProductWithInventory(selectedProduct, selectedProduct.getProduct_qty() - quantity);

        Toast.makeText(this, "Pembelian berhasil disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }

    private List<String> getProductNames(List<Product> products) {
        List<String> names = new ArrayList<>();
        for (Product product : products) {
            names.add(product.getProduct_name());
        }
        return names;
    }
}
