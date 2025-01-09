package com.example.kedaiku.UI.penjualan_menu;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.UI.product_menu.AddProductActivity;
import com.example.kedaiku.UI.product_menu.DuplicateProductActivity;
import com.example.kedaiku.UI.product_menu.ProductAdapter;
import com.example.kedaiku.UI.product_menu.UpdateProductActivity;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PilihProdukActivity extends AppCompatActivity implements SelectProdukDialog.SelectProdukDialogListener {

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;
    private List<Product> productsAsli;
    private EditText editTextSearch;
    private String csvData;
    ImageView expCsv;
    private ActivityResultLauncher<Intent> createFileLauncher;
    Map<Long, Double> productQuantityMap = new HashMap<>();
    Intent intent;
    int idxSelected=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_product);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this);
        productsAsli = new ArrayList<>();
        recyclerView.setAdapter(adapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);



        intent = getIntent();
        ArrayList<String> productIdsAndQuantities = intent.getStringArrayListExtra("productIdsAndQuantities");


        if (productIdsAndQuantities != null) {

            for (String entry : productIdsAndQuantities) {
                String[] parts = entry.split(":");
                Long productId = Long.parseLong(parts[0]);
                Double quantity = Double.parseDouble(parts[1]);
                productQuantityMap.put(productId, quantity);
            }


        }

        // Observe searchResults instead of getAllProducts
        productViewModel.getSearchResults().observe(this, products -> {
            if (products != null) {
                productsAsli=products;
                reduceStockBasedOnItems(products, productQuantityMap);
                adapter.setProductList(products);
            } else {
                productsAsli=new ArrayList<>();
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
            }
        });

        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> {
            intent = new Intent(PilihProdukActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(Product product) {
                Intent intent = new Intent(PilihProdukActivity.this, UpdateProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Product product) {
                // Tampilkan AlertDialog untuk konfirmasi
                new AlertDialog.Builder(PilihProdukActivity.this)
                        .setTitle("Konfirmasi Penghapusan")
                        .setMessage("Apakah Anda yakin ingin menghapus produk ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            // Jika pengguna memilih "Ya", hapus produk
                            productViewModel.delete(product);
                            Toast.makeText(PilihProdukActivity.this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", (dialog, which) -> {
                            // Jika pengguna memilih "Tidak", tutup dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }

            @Override
            public void onDuplicateClicked(Product product) {
                Intent intent = new Intent(PilihProdukActivity.this, DuplicateProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddStockClicked(Product product) {
                showAddStockDialog(product);
            }

            @Override
            public void onItemClicked(Product product,int idx) {
                idxSelected=idx;
                SelectProdukDialog dialog = SelectProdukDialog.newInstance(
                        product.getId(),
                        product.getProduct_name(),
                        product.getProduct_price(),
                        product.getProduct_qty(),
                        product.getProduct_primary_price(),
                        product.getProduct_unit()
                );
                dialog.show(getSupportFragmentManager(), "SelectProdukDialog");
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
        List<Product> productsToExport = productsAsli;

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

    @Override
    public void onProductSelected(long productId, String productName, double productPrice, double quantity,double productHPP,String productUnit) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("product_id", productId);
        resultIntent.putExtra("product_name", productName);
        resultIntent.putExtra("product_unit", productUnit);
        resultIntent.putExtra("product_price", productPrice);
        resultIntent.putExtra("product_stock", productsAsli.get(idxSelected).getProduct_qty());
        resultIntent.putExtra("product_HPP", productHPP);
        resultIntent.putExtra("quantity", quantity);

        setResult(RESULT_OK, resultIntent);
        finish();
    }


    private void reduceStockBasedOnItems(List<Product> products, Map<Long, Double> productQuantityMap) {
        for (Product product : products) {
            if (productQuantityMap.containsKey(product.getId())) {
                double reducedStock = product.getProduct_qty() - productQuantityMap.get(product.getId());
                product.setProduct_qty(reducedStock);

            }
        }
    }


}
