package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(Product product);

    @Transaction
    default void insertProductWithInventory(Product product, Inventory inventory, InventoryDao inventoryDao) {
        long productId = insertProductAndGetId(product);
        product.setId(productId); // Set the ID to the product object
        inventory.setStock_product_id( productId);
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


//    @Transaction
//    default void processPurchase(
//            Purchase purchase,
//            double selectedPrice,
//            Inventory inventory,
//            Product product,
//            long cashId,
//            double purchaseAmount,
//            String cashFlowDescription,
//            InventoryDao inventoryDao,
//            CashDao cashDao,
//            PurchaseDao purchaseDao
//    ) {
//        // Insert the purchase and get its ID
//        long purchaseId = purchaseDao.insertPurchaseAndGetId(purchase);
//
//        // Update cash value and insert cash flow
//        String purchaseDescription = cashFlowDescription + " id_purchase: " + purchaseId;
//        cashDao.updateCashValue(cashId, -purchaseAmount);
//        cashDao.insertCashFlow(new CashFlow(cashId, System.currentTimeMillis(), purchaseDescription, -purchaseAmount));
//
//        // Update product details
//        double oldQty = product.getProduct_qty();
//        product.setProduct_primary_price(selectedPrice);
//        product.setProduct_qty(oldQty + inventory.getStock_in());
//        update(product);
//
//        // Insert inventory record
//        inventory.setStock_product_id(product.getId());
//        inventory.setStock_note(purchaseDescription);
//        inventoryDao.insert(inventory);
//    }

    @Transaction
    default void processPurchase(
            Purchase purchase,
            double selectedPrice,
            Inventory inventory,
            Product product,
            long cashId,
            double purchaseAmount,
            String cashFlowDescription,
            InventoryDao inventoryDao,
            CashDao cashDao,
            PurchaseDao purchaseDao
    ) {
        // Fetch cash synchronously
        Cash cash = cashDao.getCashByIdSync(cashId);
        if (cash == null) {
            throw new IllegalStateException("Kas tidak ditemukan.");
        }

        // Check if the cash balance is sufficient
        if (cash.getCash_value() < purchaseAmount) {
            throw new IllegalStateException("Saldo kas tidak mencukupi untuk pembelian.");
        }

        // Deduct cash balance
        cashDao.updateCashValue(cashId, -purchaseAmount);



        // Insert the purchase and get its ID
        long purchaseId = purchaseDao.insertPurchaseAndGetId(purchase);
        // Insert cash flow for the purchase
        String purchaseDescription = cashFlowDescription + " id_purchase: " + purchaseId+ " cash_id "+cashId;
        CashFlow cashFlow = new CashFlow(cashId, System.currentTimeMillis(), purchaseDescription, -purchaseAmount);
        cashDao.insertCashFlow(cashFlow);
        // Update product details
        double oldQty = product.getProduct_qty();
        product.setProduct_primary_price(selectedPrice);
        product.setProduct_qty(oldQty + inventory.getStock_in());
        update(product);

        // Insert inventory record
        inventory.setStock_product_id(product.getId());
        inventory.setStock_note(purchaseDescription );
        inventoryDao.insert(inventory);
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

    @Query("SELECT * FROM table_product WHERE id = :productId LIMIT 1")
    Product getProductByIdSync(long productId);

    @Transaction
    default void deletePurchaseTransaction(
            Purchase purchase,
            CashDao cashDao,
            InventoryDao inventoryDao,
            ProductDao productDao,
            PurchaseDao purchaseDao
    ) {
        // Fetch cash synchronously
        Cash cash = cashDao.getCashByIdSync(purchase.getCash_id());
        if (cash == null) {
            throw new IllegalStateException("Kas tidak ditemukan.");
        }

        // Parse purchase details

        double refundAmount ;
        double price,qty;
        try {
            JSONObject jsonObject = new JSONObject(purchase.getPurchase_detail());
             price = jsonObject.getDouble("product_price");
             qty = jsonObject.getDouble("product_qty");
            refundAmount = price * qty;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        // Update cash value and insert cash flow
        cashDao.updateCashValue(purchase.getCash_id(), refundAmount);
        String cashFlowDescription = "Pembatalan pembelian produk dengan purchase id: " + purchase.get_id()+ " saldo kembali : "+refundAmount;
        CashFlow cashFlow = new CashFlow(purchase.getCash_id(), System.currentTimeMillis(), cashFlowDescription, refundAmount);
        cashDao.insertCashFlow(cashFlow);

        // Update product quantity and insert inventory flow
        Product product = productDao.getProductByIdSync(purchase.getProduct_id());
        if (product != null) {
            product.setProduct_qty(product.getProduct_qty() - qty);
            productDao.update(product);

            Inventory inventory = new Inventory(
                    System.currentTimeMillis(),
                    product.getId(),
                    "Pembatalan pembelian produk dengan purchase id: " + purchase.get_id(),
                    0, // No stock in
                    qty, // Stock out
                    product.getProduct_qty()
            );
            inventoryDao.insert(inventory);
        }

        // Delete the purchase
        purchaseDao.delete(purchase);
    }



}
