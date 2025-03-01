package com.example.kedaiku.UI.utang_menu;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.entites.ParsingHistory;
import com.example.kedaiku.helper.FormatHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatPembayaranHutangAdapter extends RecyclerView.Adapter<RiwayatPembayaranHutangAdapter.RiwayatPembayaranHutangViewHolder> {
    private List<ParsingHistory> historyList = new ArrayList<>();

    public void setHistoryList(List<ParsingHistory> list) {
        this.historyList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RiwayatPembayaranHutangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new RiwayatPembayaranHutangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatPembayaranHutangViewHolder holder, int position) {
        ParsingHistory history = historyList.get(position);
        // Format tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(history.getDate()));
        holder.textViewPaymentDate.setText(formattedDate);
        holder.textViewPaymentAmount.setText(FormatHelper.formatCurrency(history.getPaid()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class RiwayatPembayaranHutangViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPaymentDate, textViewPaymentAmount;
        public RiwayatPembayaranHutangViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPaymentDate = itemView.findViewById(R.id.textViewPaymentDate);
            textViewPaymentAmount = itemView.findViewById(R.id.textViewPaymentAmount);
        }
    }
}
