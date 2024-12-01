package com.example.kedaiku.UI.inventory_menu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.ifaceDao.ProductDao;
import com.example.kedaiku.repository.ProductRepository;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.example.kedaiku.viewmodel.PurchaseViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPurchaseDialogFragment extends DialogFragment {

    private EditText editTextDate, editTextPrice, editTextQuantity;
    private AutoCompleteTextView autoCompleteProduct;
    private Spinner spinnerCash;
    private Button buttonSave, buttonCancel;

    private PurchaseViewModel purchaseViewModel;
    private ProductViewModel productViewModel;

    private List<Product> productList;
    private Product selectedProduct;
    private long selectedDate; // String date as per Purchase entity
    private long selectedCashId;

    private List<String> cashOptions; // Daftar pilihan dari table_cash
    private CashViewModel cashViewModel;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_purchase, container, false);

        editTextDate = view.findViewById(R.id.editTextDate);
        autoCompleteProduct = view.findViewById(R.id.autoCompleteProduct);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        editTextQuantity = view.findViewById(R.id.editTextQuantity);
        spinnerCash = view.findViewById(R.id.spinnerCash);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);


        // Atur tanggal hari ini
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Sesuaikan format
        selectedDate = calendar.getTimeInMillis();
        editTextDate.setText(dateFormat.format(calendar.getTime()));

        editTextDate.setOnClickListener(v -> showDatePicker());

        // Mengisi daftar produk untuk AutoCompleteTextView
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            productList = products;
            ArrayAdapter<Product> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    products
            );
            autoCompleteProduct.setAdapter(adapter);
        });

        autoCompleteProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedProduct =  (Product)parent.getAdapter().getItem(position);
                long productId = selectedProduct.getId();

            if (selectedProduct != null) {
                editTextPrice.setText(String.valueOf(selectedProduct.getProduct_primary_price()));
            }
            }
        });





        cashViewModel.getAllCash().observe(getViewLifecycleOwner(), cashList -> {
            List<String> cashNames = new ArrayList<>();
            for (Cash cash : cashList) {
                cashNames.add(cash.getCash_name());
            }
            cashOptions = cashNames;

            ArrayAdapter<String> cashAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    cashOptions
            );
            cashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCash.setAdapter(cashAdapter);

            spinnerCash.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCashId = cashList.get(position).getId();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedCashId = 0;
                }
            });
        });
        buttonSave.setOnClickListener(v -> savePurchase());
        buttonCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        // Parse tanggal saat ini dari editTextDate
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            calendar.setTime(new Date(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = selectedCalendar.getTimeInMillis();
                    editTextDate.setText(dateFormat.format(selectedCalendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void savePurchase() {
        String productName = autoCompleteProduct.getText().toString();
        String priceText = editTextPrice.getText().toString();
        String quantityText = editTextQuantity.getText().toString();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(priceText) || TextUtils.isEmpty(quantityText)) {
            Toast.makeText(getContext(), "Semua field harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProduct == null) {
            Toast.makeText(getContext(), "Pilih produk yang valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        double quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Harga dan Jumlah harus berupa angka.", Toast.LENGTH_SHORT).show();
            return;
        }

        double oldPrice = selectedProduct.getProduct_primary_price();
        if (price != oldPrice) {
            showPriceOptionDialog(price, oldPrice, quantity);
        } else {
            completeTransaction(price, quantity);
        }
    }

    private void showPriceOptionDialog(double newPrice, double oldPrice, double quantity) {
        double averagePrice = ((oldPrice * selectedProduct.getProduct_qty()) + (newPrice * quantity)) /
                (selectedProduct.getProduct_qty() + quantity);

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_price_options, null);

        // Get references to the radio buttons
        RadioButton radioNewPrice = dialogView.findViewById(R.id.radioNewPrice);
        RadioButton radioOldPrice = dialogView.findViewById(R.id.radioOldPrice);
        RadioButton radioAveragePrice = dialogView.findViewById(R.id.radioAveragePrice);

        // Set the text dynamically to include price values
        radioNewPrice.setText(String.format(Locale.getDefault(), "Gunakan Harga Baru (%.2f)", newPrice));
        radioOldPrice.setText(String.format(Locale.getDefault(), "Gunakan Harga Lama (%.2f)", oldPrice));
        radioAveragePrice.setText(String.format(Locale.getDefault(), "Gunakan Harga Rata-Rata (%.2f)", averagePrice));

        // Show the dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Pilih Harga")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedOption = ((RadioGroup) dialogView.findViewById(R.id.radioGroupPriceOptions))
                            .getCheckedRadioButtonId();

                    double finalPrice;
                    if (selectedOption == R.id.radioNewPrice) {
                        finalPrice = newPrice;
                    } else if (selectedOption == R.id.radioOldPrice) {
                        finalPrice = oldPrice;
                    } else {
                        finalPrice = averagePrice;
                    }

                    completeTransaction(finalPrice, quantity);
                })
                .setNegativeButton("Batal", null)
                .show();
    }


    private void completeTransaction(double finalPrice, double quantity) {
        double purchaseAmount = finalPrice * quantity;



        // Prepare JSON data
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("product_price", finalPrice);
            jsonObject.put("product_qty", quantity);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal membuat data produk.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Purchase object
        Purchase purchase = new Purchase(
                selectedDate,
                selectedProduct.getId(),
                selectedCashId,
                jsonObject.toString()
        );

        // Create Inventory object
        Inventory inventory = new Inventory(
                selectedDate,
                selectedProduct.getId(),
                "",
                quantity,
                0, // No stock out
                selectedProduct.getProduct_qty() + quantity
                 // Stock note will be set in the transaction
        );

        // Execute the transaction
        executeTransaction(purchase, inventory, finalPrice, quantity, purchaseAmount);
    }

    private void executeTransaction(
            Purchase purchase,
            Inventory inventory,
            double finalPrice,
            double quantity,
            double purchaseAmount
    ) {
        productViewModel.processPurchase(
                purchase,
                finalPrice,
                inventory,
                selectedProduct,
                selectedCashId,
                purchaseAmount,
                "Pembelian produk",
                new ProductRepository.OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }






}
