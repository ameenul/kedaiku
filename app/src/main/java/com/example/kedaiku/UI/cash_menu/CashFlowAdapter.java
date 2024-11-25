package com.example.kedaiku.UI.cash_menu;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.entites.CashFlow;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CashFlowAdapter extends RecyclerView.Adapter<CashFlowAdapter.CashFlowViewHolder> {

    private Context context;
    private List<CashFlow> cashFlowList;

    public CashFlowAdapter(Context context) {
        this.context = context;
    }

    public void setCashFlowList(List<CashFlow> cashFlowList) {
        this.cashFlowList = cashFlowList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CashFlowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cash_flow, parent, false);
        return new CashFlowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CashFlowViewHolder holder, int position) {
        CashFlow cashFlow = cashFlowList.get(position);

        double amount = cashFlow.getCash_value();
        String formattedAmount = formatCurrency(Math.abs(amount));

        if (amount > 0) {
            holder.textViewAmount.setTextColor(Color.GREEN);
            holder.textViewAmount.setText("+ " + formattedAmount);
        } else if (amount < 0) {
            holder.textViewAmount.setTextColor(Color.RED);
            holder.textViewAmount.setText("- " + formattedAmount);
        } else {
            holder.textViewAmount.setTextColor(Color.BLUE);
            holder.textViewAmount.setText(formattedAmount);
        }

        holder.textViewDescription.setText(cashFlow.getCash_description());
        holder.textViewDate.setText(formatDate(cashFlow.getCash_date()));
    }

    @Override
    public int getItemCount() {
        return cashFlowList != null ? cashFlowList.size() : 0;
    }

    private String formatCurrency(double value) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return numberFormat.format(value);
    }

    private String formatDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(dateInMillis);
    }

    class CashFlowViewHolder extends RecyclerView.ViewHolder {

        TextView textViewAmount;
        TextView textViewDescription;
        TextView textViewDate;

        CashFlowViewHolder(View itemView) {
            super(itemView);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}
