package com.example.kedaiku.UI.promo_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.example.kedaiku.viewmodel.WholesaleViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddHargaGrosirDialog extends DialogFragment {

    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextWholesaleName, editTextWholesalePrice, editTextMinQty;
    private TextView textViewProductPrice, textViewHpp;
    private Button buttonSave;

    private ProductViewModel productViewModel;
    private WholesaleViewModel wholesaleViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menggunakan layout yang sudah ada
        View view = inflater.inflate(R.layout.layout_add_update_harga_grosir, container, false);

        // Initialize UI components
        autoCompleteProductName = view.findViewById(R.id.autoCompleteProductName);
        editTextWholesaleName = view.findViewById(R.id.editTextWholesaleName);
        editTextWholesalePrice = view.findViewById(R.id.editTextWholesalePrice);
        editTextMinQty = view.findViewById(R.id.editTextMinQty);
        textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
        textViewHpp = view.findViewById(R.id.textViewHpp);
        buttonSave = view.findViewById(R.id.buttonSaveHargaGrosir);

        // Set up ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        wholesaleViewModel = new ViewModelProvider(this).get(WholesaleViewModel.class);

        // Set up AutoCompleteTextView
        setupProductAutoComplete();

        // Save Button Logic
        buttonSave.setOnClickListener(v -> saveWholesale());

        return view;
    }

    private void setupProductAutoComplete() {
        productViewModel.getAllProducts().observe(this, products -> {
            List<String> productNames = new ArrayList<>();
            for (Product product : products) {
                productNames.add(product.getProduct_name());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
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
            Toast.makeText(getContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double wholesalePrice = Double.parseDouble(wholesalePriceStr);
        int minQty = Integer.parseInt(minQtyStr);

        // Create and save Wholesale object
        Wholesale wholesale = new Wholesale(0, wholesaleName, wholesalePrice, minQty, 1);
        wholesaleViewModel.insert(wholesale);

        Toast.makeText(getContext(), "Harga grosir berhasil disimpan!", Toast.LENGTH_SHORT).show();
        dismiss(); // Close the dialog
    }
}
