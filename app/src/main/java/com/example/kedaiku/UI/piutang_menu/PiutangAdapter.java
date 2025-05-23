package com.example.kedaiku.UI.piutang_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.SaleWithDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PiutangAdapter extends RecyclerView.Adapter<PiutangAdapter.PiutangViewHolder> {
    private List<SaleWithDetails> salesList = new ArrayList<>();
    private OnMoreMenuClickListener menuClickListener;

    public void setSalesList(List<SaleWithDetails> list) {
        this.salesList = list;
        notifyDataSetChanged();
    }



    public void setOnMoreMenuClickListener(OnMoreMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    @NonNull
    @Override
    public PiutangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_piutang, parent, false);
        return new PiutangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PiutangViewHolder holder, int position) {
        SaleWithDetails sale = salesList.get(position);
        holder.textViewSaleId.setText(String.valueOf(sale.getSaleId()));
        holder.textViewSaleTransactionName.setText(sale.getSaleTransactionName());

        // Format tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(sale.getSaleDate()));
        holder.textViewSaleDate.setText(formattedDate);

        holder.textViewSaleTotal.setText(String.valueOf(sale.getSaleTotal()));
        holder.textViewSalePaid.setText(String.valueOf(sale.getSalePaid()));
        holder.textViewSalePaymentType.setText(sale.getSale().getSale_payment_type() == 1 ? "Cash" : "Piutang");
        holder.textViewCustomerName.setText(sale.getCustomerName());

        // Tangani klik ImageViewMore untuk menampilkan PopupMenu
        holder.imageViewMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_piutang_item_options); // Pastikan file menu sesuai

            // Tangani klik item menu menggunakan if-else
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuClickListener == null) {
                    return false;
                }

                int itemId = menuItem.getItemId();

                if (itemId == R.id.menuHubungi) {
                    menuClickListener.onHubungiPelanggan(sale);
                    return true;
                } else if (itemId == R.id.menuDetail) {
                    menuClickListener.onDetailPenjualan(sale);
                    return true;
                } else if (itemId == R.id.menuRiwayat) {
                    menuClickListener.onRiwayatPembayaran(sale);
                    return true;
                }
                else if (itemId == R.id.menuBayar) { // Tambahkan ini
                    menuClickListener.onBayar(sale);
                    return true;
                }
                else if (itemId == R.id.menuHapus) {
                    menuClickListener.onHapus(sale);
                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });

        holder.itemView.setOnClickListener(v -> {
            menuClickListener.onBayar(sale);

        });

    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public List<SaleWithDetails> getSalesList() {
        return salesList;

    }

    static class PiutangViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSaleId;
        TextView textViewSaleTransactionName;
        TextView textViewSaleDate;
        TextView textViewSaleTotal;
        TextView textViewSalePaid;
        TextView textViewSalePaymentType;
        TextView textViewCustomerName;
        ImageView imageViewMore;

        public PiutangViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSaleId = itemView.findViewById(R.id.textViewTransactionId);
            textViewSaleTransactionName = itemView.findViewById(R.id.textViewSaleTransactionName);
            textViewSaleDate = itemView.findViewById(R.id.textViewSaleDate);
            textViewSaleTotal = itemView.findViewById(R.id.textViewHutangTotal);
            textViewSalePaid = itemView.findViewById(R.id.textViewHutangPaid);
            textViewSalePaymentType = itemView.findViewById(R.id.textViewSalePaymentType);
            textViewCustomerName = itemView.findViewById(R.id.textViewCustomerName);
            imageViewMore = itemView.findViewById(R.id.imageViewMore);
        }
    }

    public interface OnMoreMenuClickListener {
        void onHubungiPelanggan(SaleWithDetails sale);
        void onDetailPenjualan(SaleWithDetails sale);
        void onRiwayatPembayaran(SaleWithDetails sale);
        void onHapus(SaleWithDetails sale);
        void onBayar(SaleWithDetails sale);
    }


}
