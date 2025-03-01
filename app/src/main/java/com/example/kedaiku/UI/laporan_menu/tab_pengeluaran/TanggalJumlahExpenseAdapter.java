package com.example.kedaiku.UI.laporan_menu.tab_pengeluaran;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.UI.laporan_menu.TanggalJumlahItem;

import java.util.List;

public class TanggalJumlahExpenseAdapter extends RecyclerView.Adapter<TanggalJumlahExpenseAdapter.ViewHolder> {

    private List<TanggalJumlahItem> dataList;

    public TanggalJumlahExpenseAdapter(List<TanggalJumlahItem> dataList) {
        this.dataList = dataList;
    }

    public void updateData(List<TanggalJumlahItem> newData) {
        dataList = newData;
        notifyDataSetChanged();
    }

    public List<TanggalJumlahItem> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public TanggalJumlahExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tanggal_jumlah_laporan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TanggalJumlahExpenseAdapter.ViewHolder holder, int position) {
        TanggalJumlahItem item = dataList.get(position);
        holder.tvTanggal.setText(item.getTanggal());
        holder.tvJumlah.setText(item.getJumlah());
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTanggal, tvJumlah;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
        }
    }
}
