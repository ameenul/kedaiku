package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.UI.laporan_menu.TanggalJumlahItem;

import java.util.List;

public class TanggalJumlahPenjualanAdapter extends RecyclerView.Adapter<TanggalJumlahPenjualanAdapter.ViewHolder> {

    public List<TanggalJumlahItem> getDataList() {
        return dataList;
    }

    private List<TanggalJumlahItem> dataList;

    public TanggalJumlahPenjualanAdapter(List<TanggalJumlahItem> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tanggal_jumlah_laporan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TanggalJumlahItem item = dataList.get(position);
        holder.tvTanggal.setText(item.getTanggal());
        holder.tvJumlahPenjualan.setText(item.getJumlah());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Method untuk update data secara dinamis
    public void updateData(List<TanggalJumlahItem> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTanggal, tvJumlahPenjualan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvJumlahPenjualan = itemView.findViewById(R.id.tvJumlah);
        }
    }
}
