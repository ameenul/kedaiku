package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import java.util.List;

public class ProdukTerjualAdapter extends RecyclerView.Adapter<ProdukTerjualAdapter.ViewHolder> {

    public List<ProdukTerjual> getProdukList() {
        return produkList;
    }

    private List<ProdukTerjual> produkList;

    public ProdukTerjualAdapter(List<ProdukTerjual> produkList) {
        this.produkList = produkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produk_terjual, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProdukTerjual produk = produkList.get(position);
        holder.tvProductName.setText(produk.getProductName());
        holder.tvProductPrice.setText("Harga: Rp " + produk.getProductPrice());
        holder.tvQuantitySold.setText("Jumlah: " + produk.getQuantitySold());
    }

    @Override
    public int getItemCount() {
        return produkList.size();
    }

    // Method untuk update data secara dinamis
    public void updateData(List<ProdukTerjual> newProdukList) {
        produkList.clear();
        produkList.addAll(newProdukList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvQuantitySold;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantitySold = itemView.findViewById(R.id.tvQuantitySold);
        }
    }
}
