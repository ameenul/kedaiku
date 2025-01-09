package com.example.kedaiku.UI.promo_menu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.WholesaleWithProduct;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HargaGrosirAdapter extends RecyclerView.Adapter<HargaGrosirAdapter.HargaGrosirViewHolder> {

    private List<WholesaleWithProduct> dataList;
    private final OnItemClickListener listener;

    public List<WholesaleWithProduct> getWholesaleList() {return dataList;
    }

    public interface OnItemClickListener {
        void onUpdateClicked(WholesaleWithProduct wholesaleWithProduct);
        void onDeleteClicked(Wholesale wholesale);
    }

    public HargaGrosirAdapter(List<WholesaleWithProduct> dataList, OnItemClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    public void setData(List<WholesaleWithProduct> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HargaGrosirViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harga_grosir, parent, false);
        return new HargaGrosirViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HargaGrosirViewHolder holder, int position) {
        WholesaleWithProduct currentItemWholesaleWithProduct = dataList.get(position);
        Wholesale currentItemWholesale = new Wholesale(currentItemWholesaleWithProduct.getProduct_id(), currentItemWholesaleWithProduct.getName(),
                currentItemWholesaleWithProduct.getPrice(), currentItemWholesaleWithProduct.getQty(), currentItemWholesaleWithProduct.getStatus());
               currentItemWholesale.set_id(currentItemWholesaleWithProduct.get_id());

        // Bind data
        holder.textViewProductName.setText(currentItemWholesaleWithProduct.getProduct_name());
        holder.textViewWholesaleName.setText("Nama Grosir: " + currentItemWholesaleWithProduct.getName());
        holder.textViewHpp.setText("HPP: " + formatToRupiah(currentItemWholesaleWithProduct.getProduct_primary_price()));
        holder.textViewProductPrice.setText("Harga Normal: " + formatToRupiah(currentItemWholesaleWithProduct.getProduct_price()));
        holder.textViewWholesalePrice.setText("Harga Grosir: " + formatToRupiah(currentItemWholesaleWithProduct.getPrice()));
        holder.textViewMinQty.setText("Min Qty: " + currentItemWholesaleWithProduct.getQty());
        String statusText = currentItemWholesaleWithProduct.getStatus() == 1 ? "Aktif" : "Tidak Aktif";
        holder.textViewStatus.setText("Status: " + statusText);
        // Set background color based on status
        // Set background color based on status
        if (currentItemWholesaleWithProduct.getStatus() == 1) {
            holder.cardView.setCardBackgroundColor(Color.WHITE); // Background normal
        } else {
            holder.cardView.setCardBackgroundColor(Color.RED); // Background merah untuk tidak aktif
        }

        // Set item click listener to handle direct clicks
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onUpdateClicked(currentItemWholesaleWithProduct); // Pass the selected item to the update method
            }
        });

        // Set menu actions
        holder.imageViewMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.imageViewMenu);
            popupMenu.inflate(R.menu.menu_item_options);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId(); // Ambil ID item yang diklik

                if (itemId == R.id.menu_edit) {
                    if (listener != null) {
                        listener.onUpdateClicked(currentItemWholesaleWithProduct); // Tindakan untuk menu edit
                    }
                    return true;

                } else if (itemId == R.id.menu_hapus) {
                    if (listener != null) {
                        listener.onDeleteClicked(currentItemWholesale); // Tindakan untuk menu hapus
                    }
                    return true;
                }

                return false; // Jika tidak cocok dengan salah satu kondisi
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    static class HargaGrosirViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewWholesaleName;
        private final TextView textViewHpp;
        private final TextView textViewProductPrice;
        private final TextView textViewWholesalePrice;
        private final TextView textViewMinQty;

        private final ImageView imageViewMenu;
        private final TextView textViewStatus;
        private final CardView cardView;

        public HargaGrosirViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewWholesaleName = itemView.findViewById(R.id.textViewWholesaleName);
            textViewHpp = itemView.findViewById(R.id.textViewHpp);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewWholesalePrice = itemView.findViewById(R.id.textViewWholesalePrice);
            textViewMinQty = itemView.findViewById(R.id.textViewMinQty);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu); // Add this in your XML
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private String formatToRupiah(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(value);
    }
}
