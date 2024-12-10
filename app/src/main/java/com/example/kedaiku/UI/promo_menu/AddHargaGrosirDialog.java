package com.example.kedaiku.UI.promo_menu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddHargaGrosirDialog extends DialogFragment {

    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextWholesaleName, editTextWholesalePrice, editTextMinQty;
    private TextView textViewProductPrice, textViewHpp, textViewWholesalePriceFormatted;
    private RadioGroup radioGroupStatus;
    private RadioButton radioButtonActive, radioButtonInactive;
    private Button buttonSave;

    private ProductViewModel productViewModel;
    private WholesaleViewModel wholesaleViewModel;
    Product product;

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
        textViewWholesalePriceFormatted = view.findViewById(R.id.textViewWholesalePriceFormatted);
        textViewHpp = view.findViewById(R.id.textViewHpp);
        radioGroupStatus = view.findViewById(R.id.radioGroupStatus);
        radioButtonActive = view.findViewById(R.id.radioButtonActive);
        radioButtonInactive = view.findViewById(R.id.radioButtonInactive);
        buttonSave = view.findViewById(R.id.buttonSaveHargaGrosir);

        // Set up ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        wholesaleViewModel = new ViewModelProvider(this).get(WholesaleViewModel.class);

        // Set up AutoCompleteTextView
        setupProductAutoComplete();
        productViewModel.getSelectedProduct().observe(getViewLifecycleOwner(), selectedProduct -> {
            if (selectedProduct != null) {
                product = selectedProduct;
                autoCompleteProductName.setText(product.getProduct_name());
                textViewProductPrice.setText(formatToRupiah(product.getProduct_price()));
                textViewHpp.setText(formatToRupiah(product.getProduct_primary_price()));
            }
        });


        // Save Button Logic
        buttonSave.setOnClickListener(v -> saveWholesale());

        // Format Harga Grosir ke Rupiah
        editTextWholesalePrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    double value = Double.parseDouble(input);
                    textViewWholesalePriceFormatted.setText(formatToRupiah(value));
                } else {
                    textViewWholesalePriceFormatted.setText("Rp 0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private String formatToRupiah(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(value);
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

                product = getProductByName(selectedProduct, products);
                if (product != null) {
                    productViewModel.setSelectedProduct(product);
                    textViewProductPrice.setText(formatToRupiah(product.getProduct_price()));
                    textViewHpp.setText(formatToRupiah(product.getProduct_primary_price()));
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
        int status = radioGroupStatus.getCheckedRadioButtonId() == R.id.radioButtonActive ? 1 : 0;

        if (productName.isEmpty() || wholesaleName.isEmpty() || wholesalePriceStr.isEmpty() || minQtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double wholesalePrice = Double.parseDouble(wholesalePriceStr);
        int minQty = Integer.parseInt(minQtyStr);

        // Create and save Wholesale object
        if(product!=null) {
            Wholesale wholesale = new Wholesale(product.getId(), wholesaleName, wholesalePrice, minQty, status);
            wholesaleViewModel.insert(wholesale);
        }
        else {
            Toast.makeText(getContext(), "Produk Gagal Ditambahkan", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Harga grosir berhasil disimpan!", Toast.LENGTH_SHORT).show();
        dismiss(); // Close the dialog
    }
}
