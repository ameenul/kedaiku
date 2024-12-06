package com.example.kedaiku.UI.promo_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.WholesaleWithProduct;

import java.util.List;

public class HargaGrosirAdapter extends RecyclerView.Adapter<HargaGrosirAdapter.HargaGrosirViewHolder> {

    private List<WholesaleWithProduct> dataList;

    public HargaGrosirAdapter(List<WholesaleWithProduct> dataList) {
        this.dataList = dataList;
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
        WholesaleWithProduct currentItem = dataList.get(position);
        holder.textViewProductName.setText(currentItem.getProduct_name());
        holder.textViewWholesaleName.setText(currentItem.getName());
        holder.textViewHpp.setText(String.format("HPP: %.2f", currentItem.getProduct_primary_price()));
        holder.textViewProductPrice.setText(String.format("Harga Normal: %.2f", currentItem.getProduct_price()));
        holder.textViewWholesalePrice.setText(String.format("Harga Grosir: %.2f", currentItem.getPrice()));
        holder.textViewMinQty.setText(String.format("Min Qty: %d", currentItem.getQty()));
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

        public HargaGrosirViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewWholesaleName = itemView.findViewById(R.id.textViewWholesaleName);
            textViewHpp = itemView.findViewById(R.id.textViewHpp);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewWholesalePrice = itemView.findViewById(R.id.textViewWholesalePrice);
            textViewMinQty = itemView.findViewById(R.id.textViewMinQty);
        }
    }
}
