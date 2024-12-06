package com.example.kedaiku.UI.promo_menu;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.example.kedaiku.viewmodel.WholesaleViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddHargaGrosirActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextWholesaleName, editTextWholesalePrice, editTextMinQty;
    private TextView textViewProductPrice, textViewHpp;
    private Button buttonSave;

    private ProductViewModel productViewModel;
    private WholesaleViewModel wholesaleViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_update_harga_grosir);

        // Initialize UI components
        autoCompleteProductName = findViewById(R.id.autoCompleteProductName);
        editTextWholesaleName = findViewById(R.id.editTextWholesaleName);
        editTextWholesalePrice = findViewById(R.id.editTextWholesalePrice);
        editTextMinQty = findViewById(R.id.editTextMinQty);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewHpp = findViewById(R.id.textViewHpp);
        buttonSave = findViewById(R.id.buttonSaveHargaGrosir);

        // Set up ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        wholesaleViewModel = new ViewModelProvider(this).get(WholesaleViewModel.class);

        // Set up AutoCompleteTextView
        setupProductAutoComplete();

        // Save Button Logic
        buttonSave.setOnClickListener(v -> saveWholesale());
    }

    private void setupProductAutoComplete() {
        productViewModel.getAllProducts().observe(this, products -> {
            List<String> productNames = new ArrayList<>();
            for (Product product : products) {
                productNames.add(product.getProduct_name());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    productNames
            );
            autoCompleteProductName.setAdapter(adapter);

            // Set listener for product selection
            autoCompleteProductName.setOnItemClickListener((parent, view, position, id) -> {
                String selectedProduct = (String) parent.getItemAtPosition(position);
                Product product = getProductByName(selectedProduct, products);
                if (product != null) {
                    textViewProductPrice.setText(String.format("%.2f", product.getProduct_price()));
                    textViewHpp.setText(String.format("%.2f", product.getProduct_primary_price()));
                }
            });
        });
    }

    private Product getProductByName(String name, List<Product> products) {
        for (Product product : products) {
            if (product.getProduct_name().equals(name)) {
                return product;
            }
        }
        return null;
    }

    private void saveWholesale() {
        String productName = autoCompleteProductName.getText().toString().trim();
        String wholesaleName = editTextWholesaleName.getText().toString().trim();
        String wholesalePriceStr = editTextWholesalePrice.getText().toString().trim();
        String minQtyStr = editTextMinQty.getText().toString().trim();

        if (productName.isEmpty() || wholesaleName.isEmpty() || wholesalePriceStr.isEmpty() || minQtyStr.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double wholesalePrice = Double.parseDouble(wholesalePriceStr);
        int minQty = Integer.parseInt(minQtyStr);

        // Create and save Wholesale object
        Wholesale wholesale = new Wholesale(0, wholesaleName, wholesalePrice, minQty, 1);
        wholesaleViewModel.insert(wholesale);

        Toast.makeText(this, "Harga grosir berhasil disimpan!", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity
    }
}
