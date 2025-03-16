package com.example.kedaiku.UI.product_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    public ProductAdapter(Context context) {
        this.context = context;
    }

    public interface OnItemClickListener {
        void onEditClicked(Product product);
        void onDeleteClicked(Product product);
        void onDuplicateClicked(Product product);
        void onAddStockClicked(Product product);
        void onItemClicked(Product product,int idx);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProductList(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set Nama Produk
        holder.textViewName.setText(product.getProduct_name());

        // Set SKU
        holder.textViewSKU.setText("SKU: " + product.getProduct_sku());

        // Format Harga dalam Rupiah
        String primaryPrice = formatCurrency(product.getProduct_primary_price());
        String sellingPrice = formatCurrency(product.getProduct_price());
        String profit = formatCurrency(product.getProduct_price() - product.getProduct_primary_price());

        // Set Harga dan Laba
        holder.textViewProductPrimaryPrice.setText("Harga Pokok: " + primaryPrice);
        holder.textViewProductPrice.setText("Harga Jual: " + sellingPrice);
        holder.textViewProfit.setText("Laba: " + profit);

        // Set Jumlah Stok
        holder.textViewStock.setText("Stok: " + product.getProduct_qty()+" "+product.getProduct_unit());

        // Menu Opsi
        holder.imageViewOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.imageViewOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_product_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_product) {
                    listener.onEditClicked(product);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_product) {
                    listener.onDeleteClicked(product);
                    return true;
                } else if (item.getItemId() == R.id.action_duplicate_product) {
                    listener.onDuplicateClicked(product);
                    return true;
                } else if (item.getItemId() == R.id.action_add_stock) {
                    listener.onAddStockClicked(product);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Klik pada Item
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(product,position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private String formatCurrency(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(value);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewSKU, textViewProductPrimaryPrice, textViewProductPrice, textViewProfit, textViewStock;
        ImageView imageViewOptions;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewProductName);
            textViewSKU = itemView.findViewById(R.id.textViewProductSKU);
            textViewProductPrimaryPrice = itemView.findViewById(R.id.textViewProductPrimaryPrice);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProfit = itemView.findViewById(R.id.textViewProductProfit);
            textViewStock = itemView.findViewById(R.id.textViewProductStock); // Inisialisasi TextView stok
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
        }
    }
    public List<Product> getProductList() {
        return productList;
    }
}
