package com.example.kedaiku.UI.inventory_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.Product;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    private List<Purchase> purchaseList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();

    private OnDeleteClickListener onDeleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public interface OnDeleteClickListener {
        void onDelete(Purchase purchase);
    }

    public void setPurchaseList(List<Purchase> purchases) {
        this.purchaseList = purchases;
        notifyDataSetChanged();
    }

    public List<Purchase> getPurchaseList() {
        return purchaseList;
    }

    public void setProductList(List<Product> products) {
        this.productList = products;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase, parent, false);
        return new PurchaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Purchase currentPurchase = purchaseList.get(position);

        holder.textViewId.setText(String.valueOf(currentPurchase.get_id()));

        // Dapatkan nama produk dari product_id
        String productName = getProductNameById(currentPurchase.getProduct_id());


        // Hitung total dari data JSON
        try {
            JSONObject jsonObject = new JSONObject(currentPurchase.getPurchase_detail());
            double price = jsonObject.getDouble("product_price");
            double qty = jsonObject.getDouble("product_qty");
            double total = price * qty;
            holder.textViewProduct.setText(productName + " Harga : "+price+ " qty : "+qty);
            holder.textViewTotal.setText(String.valueOf(total));
        } catch (Exception e) {
            e.printStackTrace();
            holder.textViewTotal.setText("Error");
        }

        holder.buttonDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDelete(currentPurchase);
            }
        });

    }

    @Override
    public int getItemCount() {
        return purchaseList.size();
    }

    public class PurchaseViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewId, textViewProduct, textViewTotal;
        private ImageButton buttonDelete;
        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    public String getProductNameById(long productId) {
        for (Product product : productList) {
            if (product.getId() == productId) {
                return product.getProduct_name();
            }
        }
        return "Unknown Product";
    }
}
