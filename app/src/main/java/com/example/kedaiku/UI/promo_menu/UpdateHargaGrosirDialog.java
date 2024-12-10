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
import com.example.kedaiku.entites.WholesaleWithProduct;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.example.kedaiku.viewmodel.WholesaleViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UpdateHargaGrosirDialog extends DialogFragment {



    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextWholesaleName, editTextWholesalePrice, editTextMinQty;
    private TextView textViewProductName, textViewHpp, textViewWholesalePriceFormatted,textViewProductPrice;
    private RadioGroup radioGroupStatus;
    private RadioButton radioButtonActive, radioButtonInactive;
    private Button buttonUpdate;

    private WholesaleWithProduct wholesaleWithProduct;
    private WholesaleViewModel wholesaleViewModel;
    private ProductViewModel productViewModel;

   // Product product;
    private static final String ARG_PRODUCT_NAME = "product_name";
    private static final String ARG_WHOLESALE_NAME = "wholesale_name";
    private static final String ARG_HPP = "hpp";
    private static final String ARG_PRICE = "price";
    private static final String ARG_QTY = "qty";
    private static final String ARG_STATUS = "status";
    private static final String ARG_Product_ID="produk_id";
    private static final String ARG_ID = "wholesale_ID";
    private static final String ARG_WHOLESALE_PRICE = "harga_grosir";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_update_harga_grosir, container, false);

        // Initialize UI components
        autoCompleteProductName = view.findViewById(R.id.autoCompleteProductName);
        textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
        editTextWholesaleName = view.findViewById(R.id.editTextWholesaleName);
        editTextWholesalePrice = view.findViewById(R.id.editTextWholesalePrice);
        editTextMinQty = view.findViewById(R.id.editTextMinQty);
        textViewProductName = view.findViewById(R.id.autoCompleteProductName); // AutoCompleteTextView reused for Product Name
        textViewHpp = view.findViewById(R.id.textViewHpp);
        textViewWholesalePriceFormatted = view.findViewById(R.id.textViewWholesalePriceFormatted);
        radioGroupStatus = view.findViewById(R.id.radioGroupStatus);
        radioButtonActive = view.findViewById(R.id.radioButtonActive);
        radioButtonInactive = view.findViewById(R.id.radioButtonInactive);
        buttonUpdate = view.findViewById(R.id.buttonSaveHargaGrosir);

        buttonUpdate.setText("Update");

        wholesaleViewModel = new ViewModelProvider(this).get(WholesaleViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        // Populate fields if wholesaleWithProduct is available
        if (wholesaleWithProduct != null) {
            populateFields();
        }
        // Set up AutoCompleteTextView
        setupProductAutoComplete();
        productViewModel.getSelectedProduct().observe(getViewLifecycleOwner(), selectedProduct -> {
            if (selectedProduct != null) {
                wholesaleWithProduct.setProduct_id(selectedProduct.getId());
                //product = selectedProduct;
                autoCompleteProductName.setText(selectedProduct.getProduct_name());
                textViewProductPrice.setText(formatToRupiah(selectedProduct.getProduct_price()));
                textViewHpp.setText(formatToRupiah(selectedProduct.getProduct_primary_price()));
            }
        });



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

        // Handle Update Button Click
        buttonUpdate.setOnClickListener(v -> updateWholesale());
        populateFields();

        return view;
    }

    private void populateFields() {
        textViewProductName.setText(wholesaleWithProduct.getProduct_name());
        textViewHpp.setText(formatToRupiah(wholesaleWithProduct.getProduct_primary_price()));
        editTextWholesaleName.setText(wholesaleWithProduct.getName());
        editTextWholesalePrice.setText(String.valueOf(wholesaleWithProduct.getPrice()));
        editTextMinQty.setText(String.valueOf(wholesaleWithProduct.getQty()));
        textViewProductPrice.setText(formatToRupiah(wholesaleWithProduct.getProduct_price()));

        if (wholesaleWithProduct.getStatus() == 1) {
            radioButtonActive.setChecked(true);
        } else {
            radioButtonInactive.setChecked(true);
        }
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
                    productViewModel.setSelectedProduct(product);
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



    private String formatToRupiah(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(value);
    }

    private void updateWholesale() {
        String wholesaleName = editTextWholesaleName.getText().toString().trim();
        String wholesalePriceStr = editTextWholesalePrice.getText().toString().trim();
        String minQtyStr = editTextMinQty.getText().toString().trim();
        int status = radioGroupStatus.getCheckedRadioButtonId() == R.id.radioButtonActive ? 1 : 0;

        if (wholesaleName.isEmpty() || wholesalePriceStr.isEmpty() || minQtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double wholesalePrice = Double.parseDouble(wholesalePriceStr);
        int minQty = Integer.parseInt(minQtyStr);

        if(wholesaleWithProduct!=null &&wholesaleViewModel!=null) {
            wholesaleWithProduct.setName(wholesaleName);
            wholesaleWithProduct.setPrice(wholesalePrice);
            wholesaleWithProduct.setQty(minQty);
            wholesaleWithProduct.setStatus(status);
            wholesaleViewModel.update(wholesaleWithProduct.toWholeSale()); // Pastikan ada konversi ke Wholesale entity
        }
        else {
            if (wholesaleWithProduct == null) {
                Toast.makeText(getContext(), "Pilih produk yang valid.", Toast.LENGTH_SHORT).show();
                return;
            }


        }


        Toast.makeText(getContext(), "Harga grosir berhasil diperbarui!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    // Gunakan setter untuk menyimpan data WholesaleWithProduct
    public void setWholesaleWithProduct(WholesaleWithProduct wholesaleWithProduct) {
        this.wholesaleWithProduct = wholesaleWithProduct;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (wholesaleWithProduct != null) {
            outState.putLong(ARG_ID, wholesaleWithProduct.get_id());
            outState.putLong(ARG_Product_ID, wholesaleWithProduct.getProduct_id());
            outState.putString(ARG_WHOLESALE_NAME, wholesaleWithProduct.getName());
            outState.putDouble(ARG_WHOLESALE_PRICE, wholesaleWithProduct.getPrice());
            outState.putInt(ARG_QTY, wholesaleWithProduct.getQty());
            outState.putInt(ARG_STATUS, wholesaleWithProduct.getStatus());
            outState.putString(ARG_PRODUCT_NAME, wholesaleWithProduct.getProduct_name());
            outState.putDouble(ARG_PRICE, wholesaleWithProduct.getProduct_price());
            outState.putDouble(ARG_HPP, wholesaleWithProduct.getProduct_primary_price());
        }
    }



    private void restoreState(Bundle savedInstanceState) {
        wholesaleWithProduct = new WholesaleWithProduct(
                savedInstanceState.getLong(ARG_ID),
                savedInstanceState.getLong(ARG_Product_ID),
                savedInstanceState.getString(ARG_WHOLESALE_NAME),
                savedInstanceState.getDouble(ARG_WHOLESALE_PRICE),
                savedInstanceState.getInt(ARG_QTY),
                savedInstanceState.getInt(ARG_STATUS),
                savedInstanceState.getString(ARG_PRODUCT_NAME),
                savedInstanceState.getDouble(ARG_PRICE),
                savedInstanceState.getDouble(ARG_HPP)


        );
    }
}
