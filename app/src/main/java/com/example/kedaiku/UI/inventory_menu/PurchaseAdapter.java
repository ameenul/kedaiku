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
import com.example.kedaiku.entites.PurchaseWithProduct;
import com.example.kedaiku.helper.FormatHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {


    List<PurchaseWithProduct> purchaseWithProducts = new ArrayList<PurchaseWithProduct>();

    private OnDeleteClickListener onDeleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(PurchaseWithProduct purchaseWithProduct);
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setPurchaseWithProductList(List<PurchaseWithProduct> purchaseWithProducts) {
        this.purchaseWithProducts = purchaseWithProducts;
        notifyDataSetChanged();
    }

    public List<PurchaseWithProduct> getPurchaseWithProductList() {

        return this.purchaseWithProducts;
    }

    public interface OnDeleteClickListener {
        void onDelete(Purchase purchase);
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
        PurchaseWithProduct currentPurchase = purchaseWithProducts.get(position);

        holder.textViewId.setText(String.valueOf(currentPurchase._id));

        // Dapatkan nama produk dari product_id
        String productName = currentPurchase.product_name;

        // Contoh jika objek Purchase memiliki metode getPurchaseDateMillis()
        long dateMillis = currentPurchase.date;

        // Konversi long milis menjadi tanggal yang dapat dibaca
        Date date = new Date(dateMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        holder.textViewDate.setText(formattedDate);


        // Hitung total dari data JSON
        try {
            JSONObject jsonObject = new JSONObject(currentPurchase.purchase_detail);
            double price = jsonObject.getDouble("product_price");
            double qty = jsonObject.getDouble("product_qty");
            double total = price * qty;
            holder.textViewProduct.setText(productName + "\nHarga  : "+ FormatHelper.formatCurrency(price) + "\nJumlah : "+qty);
            holder.textViewTotal.setText(FormatHelper.formatCurrency(total));
        } catch (Exception e) {
            e.printStackTrace();
            holder.textViewTotal.setText("Error");
        }

        holder.buttonDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                Purchase purchase = new Purchase(currentPurchase.date, currentPurchase.product_id, currentPurchase.cash_id, currentPurchase.purchase_detail);
                purchase.set_id(currentPurchase._id);
                onDeleteClickListener.onDelete(purchase);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentPurchase);
            }
        });

    }

    @Override
    public int getItemCount() {
        return purchaseWithProducts.size();
    }

    public class PurchaseViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewId, textViewProduct, textViewTotal, textViewDate;
        private ImageButton buttonDelete;
        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewProduct = itemView.findViewById(R.id.textViewProduct);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

//    public String getProductNameById(long productId) {
//        for (Product product : productList) {
//            if (product.getId() == productId) {
//                return product.getProduct_name();
//            }
//        }
//        return "Unknown Product";
//    }
}
