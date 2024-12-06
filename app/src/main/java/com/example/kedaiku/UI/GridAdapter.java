package com.example.kedaiku.UI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.UI.cash_menu.CashListActivity;
import com.example.kedaiku.UI.customer_menu.CustomerListActivity;
import com.example.kedaiku.UI.inventory_menu.InventoryActivity;
import com.example.kedaiku.UI.product_menu.ProductListActivity;
import com.example.kedaiku.UI.promo_menu.PromoActivity;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private String[] titles = {
            "Kas", "Pelanggan", "Produk", "Inventory", "Penjualan",
            "Piutang", "Hutang", "Laporan", "Promo", "About Us", "Data"
    };

    private int[] images = {
            R.drawable.ic_cash, R.drawable.ic_pelanggan, R.drawable.ic_produk,
            R.drawable.ic_inventory, R.drawable.ic_penjualan, R.drawable.ic_piutang,
            R.drawable.ic_hutang, R.drawable.ic_laporan, R.drawable.ic_promo,
            R.drawable.ic_about_us, R.drawable.ic_data
    };
    private Context context;


    public GridAdapter(Context context) {
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int currentPosition = position;
        holder.titleTextView.setText(titles[position]);
        holder.iconImageView.setImageResource(images[position]);
        holder.iconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPosition==0) { // Assuming 0 is the position of the cash icon
                    Intent intent = new Intent(context, CashListActivity.class);
                    context.startActivity(intent);
                }
                else if (currentPosition==1) { // Assuming 0 is the position of the cash icon
                    Intent intent = new Intent(context, CustomerListActivity.class);
                    context.startActivity(intent);
                }
                else if (currentPosition==2) { // Assuming 0 is the position of the cash icon
                    Intent intent = new Intent(context, ProductListActivity.class);
                    context.startActivity(intent);
                }

                else if (currentPosition==3) { // Assuming 0 is the position of the cash icon
                    Intent intent = new Intent(context, InventoryActivity.class);
                    context.startActivity(intent);
                }

                else if (currentPosition==8) { // Assuming 0 is the position of the cash icon
                    Intent intent = new Intent(context, PromoActivity.class);
                    context.startActivity(intent);
                }

            }
        });



    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon);
            titleTextView = itemView.findViewById(R.id.title);
        }
    }
}

