package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.entites.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(Product product);

    @Transaction
    default void insertProductWithInventory(Product product, Inventory inventory, InventoryDao inventoryDao) {
        long productId = insertProductAndGetId(product);
        product.setId(productId); // Set the ID to the product object
        inventory.setStock_product_id((int) productId);
        inventoryDao.insert(inventory);
    }

    @Insert
    long insertProductAndGetId(Product product);

    @Update
    void update(Product product);

    @Transaction
    default void updateProductWithInventory(Product product, double oldQty, InventoryDao inventoryDao) {
        update(product);
        double changeInQty = product.getProduct_qty() - oldQty;

        if (changeInQty != 0) {
            // String note = (changeInQty > 0 ? "Tambah Stok: " : "Kurangi Stok: ") + Math.abs(changeInQty);
            String note = "Update jumlah barang dari " + oldQty + " ke " + product.getProduct_qty();

            Inventory inventory = new Inventory(
                    System.currentTimeMillis(),
                    product.getId(),
                    note,
                    Math.max(0, changeInQty),  // stock_in
                    Math.abs(Math.min(0, changeInQty)),  // stock_out
                    product.getProduct_qty()
            );
            inventoryDao.insert(inventory);
        }
    }


    @Transaction
    default void updateProductWithInventory(Product product, double oldQty,String stock_note ,InventoryDao inventoryDao) {
        update(product);
        double changeInQty = product.getProduct_qty() - oldQty;

        if (changeInQty != 0) {
            // String note = (changeInQty > 0 ? "Tambah Stok: " : "Kurangi Stok: ") + Math.abs(changeInQty);
            String note = stock_note;

            Inventory inventory = new Inventory(
                    System.currentTimeMillis(),
                    product.getId(),
                    note,
                    Math.max(0, changeInQty),  // stock_in
                    Math.abs(Math.min(0, changeInQty)),  // stock_out
                    product.getProduct_qty()
            );
            inventoryDao.insert(inventory);
        }
    }

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM table_product ORDER BY product_name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT p.id AS productId, " +
            "p.product_name AS productName, " +
            "SUM(i.stock_in) AS stockIn, " +
            "SUM(i.stock_out) AS stockOut, " +
            "(SELECT stock_balance FROM table_inventory WHERE stock_product_id = i.stock_product_id ORDER BY stock_date DESC LIMIT 1) AS stockBalance " +
            "FROM table_inventory i " +
            "JOIN table_product p ON i.stock_product_id = p.id " +
            "WHERE i.stock_date BETWEEN :startDate AND :endDate " +
            "GROUP BY i.stock_product_id, p.product_name, p.id")
    LiveData<List<ProductInventory>> getFilteredProductInventory(long startDate, long endDate);



    @Query("SELECT p.id AS productId, " +
            "p.product_name AS productName, " +
            "SUM(i.stock_in) AS stockIn, " +
            "SUM(i.stock_out) AS stockOut, " +
            "(SELECT stock_balance FROM table_inventory WHERE stock_product_id = i.stock_product_id ORDER BY stock_date DESC LIMIT 1) AS stockBalance " +
            "FROM table_inventory i " +
            "JOIN table_product p ON i.stock_product_id = p.id " +
            "GROUP BY i.stock_product_id, p.id, p.product_name")
    LiveData<List<ProductInventory>> getAllProductInventory();


    @Query("SELECT * FROM table_product WHERE product_name LIKE '%' || :query || '%' OR product_sku LIKE '%' || :query || '%'")
    LiveData<List<Product>> searchProducts(String query);



    @Query("SELECT * FROM table_product WHERE id = :productId LIMIT 1")
    LiveData<Product> getProductById(long productId);




}
