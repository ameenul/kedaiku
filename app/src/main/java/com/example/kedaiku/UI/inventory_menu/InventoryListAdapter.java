package com.example.kedaiku.UI.inventory_menu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Inventory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InventoryListAdapter extends RecyclerView.Adapter<InventoryListAdapter.InventoryViewHolder> {

    private List<Inventory> inventoryList;

    public void submitList(List<Inventory> list) {
        this.inventoryList = list;
        Log.d("masuk sini", "submitList: "+list.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Inventory inventory = inventoryList.get(position);

        // Format Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(inventory.getStock_date());

        holder.description.setText(formattedDate + "\n" + inventory.getStock_note());
        holder.stockIn.setText(String.format("%.2f", inventory.getStock_in()));
        holder.stockOut.setText(String.format("%.2f", inventory.getStock_out()));
        holder.stockBalance.setText(String.format("%.2f", inventory.getStock_balance()));
    }

    @Override
    public int getItemCount() {
        return inventoryList != null ? inventoryList.size() : 0;
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView description, stockIn, stockOut, stockBalance;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.textViewDescription);
            stockIn = itemView.findViewById(R.id.textViewStockIn);
            stockOut = itemView.findViewById(R.id.textViewStockOut);
            stockBalance = itemView.findViewById(R.id.textViewStockBalance);
        }
    }
}
