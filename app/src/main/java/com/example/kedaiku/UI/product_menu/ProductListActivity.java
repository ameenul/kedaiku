//package com.example.kedaiku.UI.product_menu;
//
//import android.app.Dialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.kedaiku.R;
//import com.example.kedaiku.entites.Product;
//import com.example.kedaiku.viewmodel.ProductViewModel;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class ProductListActivity extends AppCompatActivity {
//
//    private ProductViewModel productViewModel;
//    private ProductAdapter adapter;
//    private EditText editTextSearch;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_list_product);
//
//        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        adapter = new ProductAdapter(this);
//        recyclerView.setAdapter(adapter);
//
//        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
//
//        productViewModel.getAllProducts().observe(this, products -> adapter.setProductList(products));
//
//        editTextSearch = findViewById(R.id.editTextSearchProduct);
//        editTextSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String query = s.toString().trim();
//                if (!query.isEmpty()) {
//                    productViewModel.searchProducts(query).observe(ProductListActivity.this, adapter::setProductList);
//                } else {
//                    adapter.setProductList(productViewModel.getAllProducts().getValue());
//                }
//            }
//        });
//
//        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
//        fabAddProduct.setOnClickListener(v -> {
//            Intent intent = new Intent(ProductListActivity.this, AddProductActivity.class);
//            startActivity(intent);
//        });
//
//        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
//            @Override
//            public void onEditClicked(Product product) {
//                Intent intent = new Intent(ProductListActivity.this, UpdateProductActivity.class);
//                intent.putExtra("product_id", product.getId());
//                startActivity(intent);
//            }
//
//            @Override
//            public void onDeleteClicked(Product product) {
//                productViewModel.delete(product);
//                Toast.makeText(ProductListActivity.this, "Produk dihapus", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDuplicateClicked(Product product) {
//                Intent intent = new Intent(ProductListActivity.this, DuplicateProductActivity.class);
//                intent.putExtra("product_id", product.getId());
//                startActivity(intent);
//            }
//
//            @Override
//            public void onAddStockClicked(Product product) {
//                showAddStockDialog(product);
//            }
//
//            @Override
//            public void onItemClicked(Product product) {
//                Intent intent = new Intent(ProductListActivity.this, UpdateProductActivity.class);
//                intent.putExtra("product_id", product.getId());
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void showAddStockDialog(Product product) {
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.dialog_add_stock);
//
//        EditText editTextStock = dialog.findViewById(R.id.editTextStockAmount);
//        dialog.findViewById(R.id.buttonAddStock).setOnClickListener(v -> {
//            String stockInput = editTextStock.getText().toString().trim();
//
//
//            if (!stockInput.isEmpty()) {
//                double stockAmount = Double.parseDouble(stockInput);
//                double oldQty = product.getProduct_qty();
//         //       double newQty = Double.parseDouble(stockInput);
//
//
//
//                product.setProduct_qty(product.getProduct_qty() + stockAmount);
////                Inventory inventory = new Inventory(getCurrentDate(), product.getId(),
////                        "Tambah Stok: " + stockAmount, stockAmount, 0, product.getProduct_qty());
//
//                productViewModel.updateProductWithInventory(product,oldQty);
//                Toast.makeText(this, "Stok berhasil ditambahkan", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }
//
//    private String getCurrentDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        return sdf.format(new Date());
//    }
//}
//
//
//
//

package com.example.kedaiku.UI.product_menu;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;
    private EditText editTextSearch;
    private String csvData;
    ImageView expCsv;
    private  ActivityResultLauncher<Intent> createFileLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_product);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this);
        recyclerView.setAdapter(adapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Observe searchResults instead of getAllProducts
        productViewModel.getSearchResults().observe(this, products -> {
            if (products != null) {
                adapter.setProductList(products);
            } else {
                adapter.setProductList(null);
                Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
            }
        });

        editTextSearch = findViewById(R.id.editTextSearchProduct);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                productViewModel.setSearchQuery(query);
                // No need to observe here, as we already observe searchResults above
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

        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri uri = null;
                        if (result.getData() != null) {
                            uri = result.getData().getData();
                            if (uri != null) {
                                writeCsvDataToUri(uri);
                            } else {
                                Toast.makeText(this, "Gagal mendapatkan URI file", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Export dibatalkan", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        expCsv = findViewById(R.id.imageViewExportCsv);
        expCsv.setOnClickListener(v -> exportProductDataToCsv());

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

                product.setProduct_qty(product.getProduct_qty() + stockAmount);

                productViewModel.updateProductWithInventory(product, oldQty);
                Toast.makeText(this, "Stok berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    // Tambahkan metode untuk export CSV
    private void exportProductDataToCsv() {
        // Dapatkan data produk dari adapter
        List<Product> productsToExport = adapter.getProductList();

        if (productsToExport == null || productsToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data produk untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder data = new StringBuilder();

        // Header file
        data.append("Daftar Produk\n\n");

        // Header kolom
        data.append("ID,Nama Produk,Deskripsi,SKU,Harga Jual,Harga Pokok,Unit,Stok,Laba\n");

        // Format data produk
        for (Product product : productsToExport) {
            data.append(product.getId()).append(","); // ID
            data.append(product.getProduct_name().replace(",", " ")).append(","); // Nama Produk
            data.append(product.getProduct_description().replace(",", " ")).append(","); // Deskripsi
            data.append(product.getProduct_sku() != null ? product.getProduct_sku().replace(",", " ") : "-").append(","); // SKU
            data.append(product.getProduct_price()).append(","); // Harga
            // Jenis Harga (misalnya "Harga Jual")
            data.append(product.getProduct_primary_price()).append(","); // Harga Pokok
            data.append(product.getProduct_unit().replace(",", " ")).append(","); // Unit
            data.append(product.getProduct_qty()).append(","); // Stok
            data.append(product.getProduct_price()-product.getProduct_primary_price()).append("\n");
        }

        // Simpan data CSV ke variabel
        csvData = data.toString();

        // Buat intent untuk memilih lokasi penyimpanan file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Daftar_Produk.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes()); // Tulis data CSV ke output
            outputStream.close();
            Toast.makeText(this, "Data produk berhasil diekspor", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
