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
import com.example.kedaiku.entites.SpecialPriceWithProduct;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.example.kedaiku.viewmodel.SpecialPriceViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UpdateHargaKhususDialog extends DialogFragment {

    private AutoCompleteTextView autoCompleteProductName;
    private EditText editTextSpecialPriceName, editTextDiscountPercentage;
    private TextView textViewProductPrice, textViewHpp, textViewDiscountAmount, textViewSpecialPrice;
    private Spinner spinnerMember;
    private RadioGroup radioGroupStatus;
    private RadioButton radioButtonActive, radioButtonInactive;
    private Button buttonUpdate;

    private ProductViewModel productViewModel;
    private SpecialPriceViewModel specialPriceViewModel;
    private CustomerGroupViewModel customerGroupViewModel;

    private SpecialPriceWithProduct specialPriceWithProduct;

    private List<CustomerGroup> customerGroups = new ArrayList<>();
    long idGroup= 0;

  //  private SpecialPrice specialPrice;
 //   private Product product;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_update_harga_khusus, container, false);

        //kalau rotasi set spesilpricewithproduk
        if (savedInstanceState != null) {
            SpecialPrice specialPrice = new SpecialPrice(
                    savedInstanceState.getString("name"),
                    savedInstanceState.getLong("product_id"),
                    savedInstanceState.getLong("group_id"),
                    savedInstanceState.getDouble("discount"),
                    savedInstanceState.getInt("status")
            );
            specialPrice.set_id(savedInstanceState.getLong("specialPrice_id"));

            Product product = new Product(
                    savedInstanceState.getString("product_name"),
                    savedInstanceState.getString("product_description"),
                    savedInstanceState.getString("product_sku"),
                    savedInstanceState.getDouble("product_price"),
                    savedInstanceState.getDouble("product_primary_price"),
                    savedInstanceState.getString("product_unit"),
                    savedInstanceState.getDouble("product_qty")
            );
            product.setId(savedInstanceState.getLong("product_id"));
            product.setBarcode(savedInstanceState.getString("barcode"));

            specialPriceWithProduct = new SpecialPriceWithProduct(specialPrice, product);

        }

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
        buttonUpdate = view.findViewById(R.id.buttonSaveSpecialPrice);

        buttonUpdate.setText("Update");

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
                groupNames.add(group.getName());
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    groupNames
            );
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMember.setAdapter(groupAdapter);

            //setup spinner awal
            if(specialPriceWithProduct!=null){
            for (int i = 0; i < spinnerMember.getAdapter().getCount(); i++) {
                long id= getGroupIdByName(spinnerMember.getAdapter().getItem(i).toString());
                if (id==(specialPriceWithProduct.getGroup_id())) {
                    spinnerMember.setSelection(i);
                    break;
                }
            }}


        });

        // Set up AutoCompleteTextView for Product ini nyambung sama vie bawah ini
        setupProductAutoComplete();
        productViewModel.getSelectedProduct().observe(getViewLifecycleOwner(), selectedProduct -> {
            if (selectedProduct != null) {
                if(specialPriceWithProduct!=null)
                {
                    Toast.makeText(getContext(), "specialPriceWithProduct ada", Toast.LENGTH_SHORT).show();
                    specialPriceWithProduct.setProduct(selectedProduct);
                    specialPriceWithProduct.getSpecialPrice().setProduct_id(selectedProduct.getId());
                }
                else{
                    Toast.makeText(getContext(), "specialPriceWithProduct kosong getselectedproduk", Toast.LENGTH_SHORT).show();
                }
                //update harga normal, hpp
                textViewHpp.setText(formatToRupiah(specialPriceWithProduct.getProduct_primary_price()));
                textViewProductPrice.setText(formatToRupiah(specialPriceWithProduct.getProduct_price()));
                updateSpecialPriceDanDiskon("selectedProduk");
            }
        });

        // Populate fields if specialPrice is available
        if (specialPriceWithProduct != null) {
            populateFields();
        }

        // Update button click listener
        buttonUpdate.setOnClickListener(v -> updateSpecialPrice());

        // TextWatcher for discount percentage
        editTextDiscountPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSpecialPriceDanDiskon("edittext persen");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
//        showKeyboard(autoCompleteProductName);
        return view;
    }

    private void populateFields() {
        autoCompleteProductName.setText(specialPriceWithProduct.getProduct_name());
        editTextSpecialPriceName.setText(specialPriceWithProduct.getName());
        textViewProductPrice.setText(formatToRupiah(specialPriceWithProduct.getProduct_price()));
        textViewHpp.setText(formatToRupiah(specialPriceWithProduct.getProduct_primary_price()));
        editTextDiscountPercentage.setText(String.valueOf(specialPriceWithProduct.getPrecent()));


        updateSpecialPriceDanDiskon("populate field");

        if (specialPriceWithProduct.getStatus() == 1) {
            radioButtonActive.setChecked(true);
        } else {
            radioButtonInactive.setChecked(true);
        }



        idGroup = specialPriceWithProduct.getGroup_id();
    }

    @NonNull
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
                Product product = getProductByName(selectedProduct, products);
                if (product != null) {
                    productViewModel.setSelectedProduct(product);
                }
            });
        });


    }



    @Nullable
    private Product getProductByName(String name, @NonNull List<Product> products) {
        for (Product product : products) {
            if (product.getProduct_name().equals(name)) {
                return product;
            }
        }
        return null;
    }

    private void updateSpecialPriceDanDiskon(String asal) {
        if (specialPriceWithProduct == null) {
            Toast.makeText(getContext(), "specialPriceWithProduct kosong ui "+asal, Toast.LENGTH_SHORT).show();
            return;
        }
        String discountStr = editTextDiscountPercentage.getText().toString().trim();
        double discount = discountStr.isEmpty() ? 0 : Double.parseDouble(discountStr);
        double productPrice = specialPriceWithProduct.getProduct_price();
        double discountAmount = productPrice * (discount / 100);
        double specialPriceValue = productPrice - discountAmount;

        textViewDiscountAmount.setText(formatToRupiah(discountAmount));
        textViewSpecialPrice.setText(formatToRupiah(specialPriceValue));
    }

    private void updateSpecialPrice() {
        if (specialPriceWithProduct==null) {
            Toast.makeText(getContext(), "specialPriceWithProduct kosong update", Toast.LENGTH_SHORT).show();
            return;
        }
        String productName = autoCompleteProductName.getText().toString().trim();
        String specialPriceName = editTextSpecialPriceName.getText().toString().trim();
        String discountStr = editTextDiscountPercentage.getText().toString().trim();
        int status = radioGroupStatus.getCheckedRadioButtonId() == R.id.radioButtonActive ? 1 : 0;

        if (productName.isEmpty() || specialPriceName.isEmpty() || discountStr.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

//        double productPrice = product.getProduct_price();
        double discount = discountStr.isEmpty() ? 0 : Double.parseDouble(discountStr);
         long id= getGroupIdByName(spinnerMember.getSelectedItem().toString());

        // Update SpecialPrice object
        SpecialPrice specialPrice = specialPriceWithProduct.getSpecialPrice();
        specialPrice.setName(specialPriceName);
        specialPrice.setPercent(discount);
        specialPrice.setStatus(status);
        specialPrice.setGroup_id(id);



        // Update in ViewModel
        specialPriceViewModel.update(specialPrice);

        Toast.makeText(getContext(), "Harga khusus berhasil diperbarui!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void setSpecialPriceWithProduct(SpecialPriceWithProduct specialPriceWithProduct) {
        this.specialPriceWithProduct = specialPriceWithProduct;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (specialPriceWithProduct != null && specialPriceWithProduct.getSpecialPrice() != null && specialPriceWithProduct.getProduct() != null) {
            SpecialPrice specialPrice = specialPriceWithProduct.getSpecialPrice();
            Product product = specialPriceWithProduct.getProduct();

            outState.putLong("specialPrice_id", specialPrice.get_id());
            outState.putString("name", specialPrice.getName());
            outState.putDouble("discount", specialPrice.getPercent());
            outState.putInt("status", specialPrice.getStatus());
            outState.putLong("group_id", specialPrice.getGroup_id());
            outState.putLong("product_id", product.getId());
            outState.putString("product_name", product.getProduct_name());
            outState.putString("product_description", product.getProduct_description());
            outState.putString("product_sku", product.getProduct_sku());
            outState.putDouble("product_price", product.getProduct_price());
            outState.putDouble("product_primary_price", product.getProduct_primary_price());
            outState.putString("product_unit", product.getProduct_unit());
            outState.putDouble("product_qty", product.getProduct_qty());
            outState.putString("barcode", product.getBarcode());
        }
    }



    double hitungDiskon(double diskon,double harganormal)
    {
        return (100-diskon)/100*harganormal;

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


    @NonNull
    private String getGroupNameByid(long id) {
        for (CustomerGroup group : customerGroups) {
            if (group.getId()==id) {
                return group.getName();  // Return the groupId of the selected group
            } else if (id==0) {
                return "Seluruh Member";
            }
        }
        return "";  // Return -1 if no matching group is found
    }


//    public void showKeyboard(@NonNull AutoCompleteTextView autoCompleteTextView) {
//        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);
//            }
//        });
//    }


}
