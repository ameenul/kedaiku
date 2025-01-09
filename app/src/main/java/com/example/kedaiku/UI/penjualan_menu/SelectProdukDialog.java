package com.example.kedaiku.UI.penjualan_menu;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kedaiku.R;

public class SelectProdukDialog extends DialogFragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private static final String ARG_PRODUCT_NAME = "product_name";
    private static final String ARG_PRODUCT_PRICE = "product_price";
    private static final String ARG_PRODUCT_STOCK = "product_stock";
    private static final String ARG_PRODUCT_HPP = "product_HPP";
    private static final String ARG_PRODUCT_UNIT = "product_unit";

    private long productId;
    private String productName;
    private String productUnit;
    private double productPrice;
    private double productStock;
    private double productHPP;

    public interface SelectProdukDialogListener {
        void onProductSelected(long productId, String productName, double productPrice, double quantity,double productHPP,String productUnit);
    }

    private SelectProdukDialogListener listener;

    public static SelectProdukDialog newInstance(long productId, String productName, double productPrice, double productStock, double productHPP, String productUnit) {
        SelectProdukDialog fragment = new SelectProdukDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, productName);
        args.putDouble(ARG_PRODUCT_PRICE, productPrice);
        args.putDouble(ARG_PRODUCT_STOCK, productStock);
        args.putDouble(ARG_PRODUCT_HPP,productHPP);
        args.putString(ARG_PRODUCT_UNIT,productUnit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SelectProdukDialogListener) {
            listener = (SelectProdukDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SelectProdukDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_pilih_produk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            productId = getArguments().getLong(ARG_PRODUCT_ID);
            productName = getArguments().getString(ARG_PRODUCT_NAME);
            productPrice = getArguments().getDouble(ARG_PRODUCT_PRICE);
            productStock = getArguments().getDouble(ARG_PRODUCT_STOCK);
            productHPP = getArguments().getDouble(ARG_PRODUCT_HPP);
            productUnit = getArguments().getString(ARG_PRODUCT_UNIT);

        }

        TextView productNameTextView = view.findViewById(R.id.textViewProductName);
        TextView productPriceTextView = view.findViewById(R.id.textViewProductPrice);
        TextView productStockTextView = view.findViewById(R.id.textViewProductStock);
        EditText quantityEditText = view.findViewById(R.id.editTextQuantity);
        Button confirmButton = view.findViewById(R.id.buttonConfirm);

        productNameTextView.setText(productName);
        productPriceTextView.setText("Harga Normal: Rp " + productPrice);
        productStockTextView.setText("Stock: " + productStock);

        confirmButton.setOnClickListener(v -> {
            String quantityStr = quantityEditText.getText().toString().trim();
            if (!quantityStr.isEmpty()) {
                double quantity = Double.parseDouble(quantityStr);
                if(quantity<=productStock){

                listener.onProductSelected(productId, productName, productPrice, quantity,productHPP,productUnit);
                dismiss();}
                else {
                    Toast.makeText(getContext(), "Stock tidak cukup", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(getContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
