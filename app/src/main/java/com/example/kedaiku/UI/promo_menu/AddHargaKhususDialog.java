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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.example.kedaiku.viewmodel.SpecialPriceViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddHargaKhususDialog extends DialogFragment {

    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextSpecialPriceName, editTextDiscountPercentage;
    private TextView textViewProductPrice, textViewHpp, textViewDiscountAmount, textViewSpecialPrice;
    private Spinner spinnerMember;
    private RadioGroup radioGroupStatus;
    private RadioButton radioButtonActive, radioButtonInactive;
    private Button buttonSave;

    private ProductViewModel productViewModel;
    private SpecialPriceViewModel specialPriceViewModel;
    private CustomerGroupViewModel customerGroupViewModel;
    private Product product;
    long groupId;

    private List<CustomerGroup> customerGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_update_harga_khusus, container, false);

        // Initialize UI components
        autoCompleteProductName = view.findViewById(R.id.autoCompleteProductName);
        editTextSpecialPriceName = view.findViewById(R.id.editTextWholesaleName);
        editTextDiscountPercentage = view.findViewById(R.id.editTextDiscountPercentage);
        textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
        textViewHpp = view.findViewById(R.id.textViewHpp);
        textViewDiscountAmount = view.findViewById(R.id.textViewDiscountAmount);
        textViewSpecialPrice = view.findViewById(R.id.textViewSpecialPrice);
        spinnerMember = view.findViewById(R.id.spinnerMember);
        radioGroupStatus = view.findViewById(R.id.radioGroupStatus);
        radioButtonActive = view.findViewById(R.id.radioButtonActive);
        radioButtonInactive = view.findViewById(R.id.radioButtonInactive);
        buttonSave = view.findViewById(R.id.buttonSaveSpecialPrice);

        // Set up ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        specialPriceViewModel = new ViewModelProvider(this).get(SpecialPriceViewModel.class);
        customerGroupViewModel = new ViewModelProvider(this).get(CustomerGroupViewModel.class);

        // Load Customer Groups for Spinner
        customerGroupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            customerGroups.clear();
            customerGroups.addAll(groups);

            List<String> groupNames = new ArrayList<>();
            groupNames.add("Seluruh Member");
            for (CustomerGroup group : customerGroups) {
                groupNames.add(group.getName());  // assuming 'getGroupName()' is a method in CustomerGroup class
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    groupNames
            );
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMember.setAdapter(groupAdapter);
        });

        // Set up AutoCompleteTextView for Product
        setupProductAutoComplete();
        productViewModel.getSelectedProduct().observe(getViewLifecycleOwner(), selectedProduct -> {
            if (selectedProduct != null) {
                product = selectedProduct;
                autoCompleteProductName.setText(product.getProduct_name());
                textViewProductPrice.setText(formatToRupiah(product.getProduct_price()));
                textViewHpp.setText(formatToRupiah(product.getProduct_primary_price()));
                updateSpecialPrice();
            }
        });

        // Save button click listener
        buttonSave.setOnClickListener(v -> saveSpecialPrice());

        // TextWatcher for discount percentage
        editTextDiscountPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSpecialPrice();
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

            autoCompleteProductName.setOnItemClickListener((parent, view, position, id) -> {
                String selectedProduct = (String) parent.getItemAtPosition(position);
                product = getProductByName(selectedProduct, products);
                if (product != null) {
                    productViewModel.setSelectedProduct(product);
                    textViewProductPrice.setText(formatToRupiah(product.getProduct_price()));
                    textViewHpp.setText(formatToRupiah(product.getProduct_primary_price()));
                    updateSpecialPrice();
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

    private void updateSpecialPrice() {
        if (product == null) {
            return;
        }
        String discountStr = editTextDiscountPercentage.getText().toString().trim();
        double discount = discountStr.isEmpty() ? 0 : Double.parseDouble(discountStr);
        double productPrice = product.getProduct_price();
        double discountAmount = productPrice * (discount / 100);
        double specialPrice = productPrice - discountAmount;

        textViewDiscountAmount.setText(formatToRupiah(discountAmount));
        textViewSpecialPrice.setText(formatToRupiah(specialPrice));
    }

    private void saveSpecialPrice() {
        if (product != null &&groupId!=-1) {
        String productName = autoCompleteProductName.getText().toString().trim();
        String specialPriceName = editTextSpecialPriceName.getText().toString().trim();
        String discountStr = editTextDiscountPercentage.getText().toString().trim();
        int status = radioGroupStatus.getCheckedRadioButtonId() == R.id.radioButtonActive ? 1 : 0;

        if (productName.isEmpty() || specialPriceName.isEmpty() || discountStr.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double discount = Double.parseDouble(discountStr);
        double discountAmount = product.getProduct_price() * (discount / 100);
        double specialPrice = product.getProduct_price() - discountAmount;

        // Get selected group name from Spinner
        String selectedGroupName = (String) spinnerMember.getSelectedItem();
          // Get the groupId based on the selected group name
            groupId = getGroupIdByName(selectedGroupName);

            // Create and save SpecialPrice object

            SpecialPrice specialPriceObj = new SpecialPrice(
                    specialPriceName,
                    product.getId(),
                    groupId,  // Use the groupId here
                            discount,
                    status
            );

            specialPriceViewModel.insert(specialPriceObj);
            Toast.makeText(getContext(), "Harga khusus berhasil disimpan!", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Produk atau Grup gagal ditambahkan", Toast.LENGTH_SHORT).show();
        }
    }

    private long getGroupIdByName(String groupName) {
        for (CustomerGroup group : customerGroups) {
            if (group.getName().equals(groupName)) {
                return group.getId();  // Return the groupId of the selected group
            } else if (groupName.equals("Seluruh Member")) {
                return 0;
            }
        }
        return -1;  // Return -1 if no matching group is found
    }

}
