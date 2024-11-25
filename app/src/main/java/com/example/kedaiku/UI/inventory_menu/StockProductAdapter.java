package com.example.kedaiku.UI.inventory_menu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.ProductInventory;

import java.util.List;

public class StockProductAdapter extends RecyclerView.Adapter<StockProductAdapter.StockProductViewHolder> {

    public List<ProductInventory> getProductInventoryList() {
        return productInventoryList;
    }

    private List<ProductInventory> productInventoryList;
    private Context context;

    // Constructor to get the context
    public StockProductAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<ProductInventory> list) {
        this.productInventoryList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_product, parent, false);
        return new StockProductViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull StockProductViewHolder holder, int position) {
//        StockProduct stockProduct = stockProductList.get(position);
//        if (stockProduct != null) {
//            holder.productName.setText(stockProduct.getProductName());
//            holder.stockIn.setText(String.format("%.2f", stockProduct.getStockIn()));
//            holder.stockOut.setText(String.format("%.2f", stockProduct.getStockOut()));
//            holder.stockBalance.setText(String.format("%.2f", stockProduct.getStockBalance()));
//
//            // Set OnClickListener for the item
//            holder.itemView.setOnClickListener(v -> {
//                Intent intent = new Intent(context, InventoryListActivity.class);
//                intent.putExtra("product_id", stockProduct.getProductId()); // Pass product ID to next activity
//                intent.putExtra("product_name", stockProduct.getProductName()); // Pass product name
//                context.startActivity(intent);
//            });
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull StockProductViewHolder holder, int position) {
        ProductInventory productInventory = productInventoryList.get(position);
        if (productInventory != null) {
            holder.productName.setText(productInventory.getProductName());
            holder.stockIn.setText(String.format("%.2f", productInventory.getStockIn()));
            holder.stockOut.setText(String.format("%.2f", productInventory.getStockOut()));
            holder.stockBalance.setText(String.format("%.2f", productInventory.getStockBalance()));

            // Set OnClickListener for the item
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, InventoryListActivity.class);
                intent.putExtra("product_id", productInventory.getProductId());
                intent.putExtra("product_name", productInventory.getProductName());
                context.startActivity(intent);
            });

            // Set background color for alternating rows
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.row_even)); // Even row color
            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.row_odd)); // Odd row color
            }
        }
    }


    @Override
    public int getItemCount() {
        return productInventoryList != null ? productInventoryList.size() : 0;
    }

    static class StockProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, stockIn, stockOut, stockBalance;

        public StockProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewProductName);
            stockIn = itemView.findViewById(R.id.textViewStockIn);
            stockOut = itemView.findViewById(R.id.textViewStockOut);
            stockBalance = itemView.findViewById(R.id.textViewStockBalance);
        }
    }
}
