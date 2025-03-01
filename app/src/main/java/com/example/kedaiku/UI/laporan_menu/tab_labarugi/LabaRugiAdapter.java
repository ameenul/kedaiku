package com.example.kedaiku.UI.laporan_menu.tab_labarugi;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.UI.laporan_menu.TanggalJumlahItem;
import com.example.kedaiku.helper.FormatHelper;


import java.util.List;

public class LabaRugiAdapter extends RecyclerView.Adapter<LabaRugiAdapter.ViewHolder> {

    private List<TanggalJumlahItem> reportList;

    public LabaRugiAdapter(List<TanggalJumlahItem> reportList) {
        this.reportList = reportList;
    }

    public void updateData(List<TanggalJumlahItem> newData) {
        this.reportList = newData;
        notifyDataSetChanged();
    }

    public List<TanggalJumlahItem> getDataList() {
        return reportList;
    }

    @NonNull
    @Override
    public LabaRugiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tanggal_jumlah_laporan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabaRugiAdapter.ViewHolder holder, int position) {
        TanggalJumlahItem report = reportList.get(position);
        holder.tvDate.setText(report.getTanggal());
        holder.tvProfitLoss.setText(report.getJumlah());
    }

    @Override
    public int getItemCount() {
        return reportList == null ? 0 : reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvProfitLoss;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvTanggal);
            tvProfitLoss = itemView.findViewById(R.id.tvJumlah);
        }
    }
}
