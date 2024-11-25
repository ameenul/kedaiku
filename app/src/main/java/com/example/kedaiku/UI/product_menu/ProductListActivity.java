package com.example.kedaiku.UI.product_menu;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;
    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_product);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this);
        recyclerView.setAdapter(adapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        productViewModel.getAllProducts().observe(this, products -> adapter.setProductList(products));

        editTextSearch = findViewById(R.id.editTextSearchProduct);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    productViewModel.searchProducts(query).observe(ProductListActivity.this, adapter::setProductList);
                } else {
                    adapter.setProductList(productViewModel.getAllProducts().getValue());
                }
            }
        });

        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(Product product) {
                Intent intent = new Intent(ProductListActivity.this, UpdateProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Product product) {
                productViewModel.delete(product);
                Toast.makeText(ProductListActivity.this, "Produk dihapus", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDuplicateClicked(Product product) {
                Intent intent = new Intent(ProductListActivity.this, DuplicateProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddStockClicked(Product product) {
                showAddStockDialog(product);
            }

            @Override
            public void onItemClicked(Product product) {
                Intent intent = new Intent(ProductListActivity.this, UpdateProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }
        });
    }

    private void showAddStockDialog(Product product) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_stock);

        EditText editTextStock = dialog.findViewById(R.id.editTextStockAmount);
        dialog.findViewById(R.id.buttonAddStock).setOnClickListener(v -> {
            String stockInput = editTextStock.getText().toString().trim();


            if (!stockInput.isEmpty()) {
                double stockAmount = Double.parseDouble(stockInput);
                double oldQty = product.getProduct_qty();
         //       double newQty = Double.parseDouble(stockInput);



                product.setProduct_qty(product.getProduct_qty() + stockAmount);
//                Inventory inventory = new Inventory(getCurrentDate(), product.getId(),
//                        "Tambah Stok: " + stockAmount, stockAmount, 0, product.getProduct_qty());

                productViewModel.updateProductWithInventory(product,oldQty);
                Toast.makeText(this, "Stok berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}



//class AddStockDialog extends Dialog {
//
//    private final Product product;
//    private final OnStockUpdateListener listener;
//    private ProductViewModel productViewModel;
//
//    public AddStockDialog(@NonNull Context context, Product product, OnStockUpdateListener listener) {
//        super(context);
//        this.product = product;
//        this.listener = listener;
//        productViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(ProductViewModel.class);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_add_stock);
//
//        EditText editTextStock = findViewById(R.id.editTextStockAmount);
//        Button buttonAddStock = findViewById(R.id.buttonAddStock);
//
//        buttonAddStock.setOnClickListener(v -> {
//            String stockInput = editTextStock.getText().toString().trim();
//            if (!TextUtils.isEmpty(stockInput)) {
//                double stockAmount = Double.parseDouble(stockInput);
//
//                // Update stok produk
//                double newQty = product.getProduct_qty() + stockAmount;
//                product.setProduct_qty(newQty);
//
//                // Tambahkan ke tabel Inventory
//                Inventory inventory = new Inventory(
//                        getCurrentDate(),
//                        (int) product.getId(),
//                        "Tambah Stok: " + stockAmount,
//                        stockAmount,
//                        0,
//                        newQty
//                );
//
//                // Perbarui produk dan inventori dalam transaksi
//                productViewModel.updateProductWithInventory(product,product.getProduct_qty() );
//
//                // Notifikasi listener dan tutup dialog
//                listener.onStockUpdated();
//                dismiss();
//                Toast.makeText(getContext(), "Stok berhasil ditambahkan", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getContext(), "Jumlah stok tidak boleh kosong!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private String getCurrentDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        return sdf.format(new Date());
//    }
//
//    public interface OnStockUpdateListener {
//        void onStockUpdated();
//    }
//}
