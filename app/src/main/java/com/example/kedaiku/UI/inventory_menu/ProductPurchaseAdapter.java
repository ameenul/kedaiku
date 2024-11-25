package com.example.kedaiku.UI.inventory_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;

import java.util.List;

public class ProductPurchaseAdapter extends RecyclerView.Adapter<ProductPurchaseAdapter.ProductPurchaseViewHolder> {

    private List<Product> productList;
    private OnItemClickListener onItemClickListener;

    public void submitList(List<Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductPurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_purchase, parent, false);
        return new ProductPurchaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductPurchaseViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getProduct_name());
        holder.productSKU.setText(String.format("SKU: %s", product.getProduct_sku()));
        holder.productPrice.setText(String.format("Harga: Rp %.2f", product.getProduct_price()));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    static class ProductPurchaseViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productSKU, productPrice;

        public ProductPurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewProductName);
            productSKU = itemView.findViewById(R.id.textViewProductSKU);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
        }
    }
}
