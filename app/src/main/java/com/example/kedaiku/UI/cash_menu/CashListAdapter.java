package com.example.kedaiku.UI.cash_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Cash;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CashListAdapter extends RecyclerView.Adapter<CashListAdapter.CashViewHolder> {
    private List<Cash> cashList = new ArrayList<>();
    private OnCashItemClickListener listener;
    private Context context;

    public CashListAdapter(Context context, OnCashItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCashList(List<Cash> cashList) {
        this.cashList = cashList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cash, parent, false);
        return new CashViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CashViewHolder holder, int position) {
        Cash cash = cashList.get(position);
        holder.textViewCashName.setText(cash.getCash_name());
        holder.textViewCashValue.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(cash.getCash_value()));

        holder.itemView.setOnClickListener(v -> listener.onCashItemClick(cash));

        holder.buttonMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.buttonMoreOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_cash_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_increase_cash) {
                    listener.onIncreaseCashClick(cash);
                    return true;
                } else if (item.getItemId() == R.id.action_decrease_cash) {
                    listener.onDecreaseCashClick(cash);
                    return true;
                } else if (item.getItemId() == R.id.action_transfer_cash) {
                    listener.onTransferCashClick(cash);
                    return true;
                } else if (item.getItemId() == R.id.action_rename_cash) {
                    listener.onRenameCashClick(cash);
                    return true;
                } else if (item.getItemId() == R.id.action_view_detail) {
                    listener.onViewDetailClick(cash);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_cash) {
                    listener.onDeleteCashClick(cash);
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return cashList.size();
    }

    public static class CashViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCashName, textViewCashValue;
        ImageButton buttonMoreOptions;

        public CashViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCashName = itemView.findViewById(R.id.textViewCashName);
            textViewCashValue = itemView.findViewById(R.id.textViewCashValue);
            buttonMoreOptions = itemView.findViewById(R.id.buttonMoreOptions);
        }
    }

    public interface OnCashItemClickListener {
        void onCashItemClick(Cash cash);
        void onIncreaseCashClick(Cash cash);
        void onDecreaseCashClick(Cash cash);
        void onTransferCashClick(Cash cash);
        void onRenameCashClick(Cash cash);
        void onViewDetailClick(Cash cash);
        void onDeleteCashClick(Cash cash);
    }
}
