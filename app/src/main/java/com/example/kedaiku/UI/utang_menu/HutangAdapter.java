package com.example.kedaiku.UI.utang_menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.helper.FormatHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HutangAdapter extends RecyclerView.Adapter<HutangAdapter.DebtViewHolder> {

    private List<DebtWithCreditor> debtList = new ArrayList<>();

    // Listener untuk menangani event di item
    private OnItemClickListener listener;

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_hutang.xml
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hutang, parent, false);
        return new DebtViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        DebtWithCreditor currentDebt = debtList.get(position);

        // Menampilkan data ke tampilan:

        // 1. Sale ID (digunakan _id di entity Debt)
        holder.textViewTransactionId.setText(String.valueOf(currentDebt.debt.get_id()));

        // 2. Transaction Note -> debt_note
        holder.textViewHutangTransactionName.setText(currentDebt.debt.getDebt_note());

        // 3. Date -> Belum ada di entity Debt, kita tampilkan "-" saja
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentDebt.debt.getDebt_date()));
        holder.textViewHutangDate.setText(formattedDate);

        // 4. Total -> debt_quantity
        // holder.textViewHutangTotal.setText(String.valueOf(currentDebt.getDebt_quantity()));
        holder.textViewHutangTotal.setText(FormatHelper.formatCurrency(currentDebt.debt.getDebt_quantity()));

        // 5. Paid -> debt_paid
       // holder.textViewHutangPaid.setText(String.valueOf(currentDebt.getDebt_paid()));
        holder.textViewHutangPaid.setText(FormatHelper.formatCurrency(currentDebt.debt.getDebt_paid()));



        // 7. Creditor Name -> Belum ada nama, hanya creditur_id => Tampilkan ID
        String creditorInfo =  currentDebt.creditor.getCreditor_name();
        holder.textViewCreditorName.setText(creditorInfo);

        // Klik ikon More (3 titik)
        holder.imageViewMore.setOnClickListener(view -> {
            // Tampilkan PopupMenu
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.menu_hutang_item_options);

            popup.setOnMenuItemClickListener(menuItem -> {
                if (listener == null) {
                    return false;
                }
                // Cek item menu yang di-klik
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menuHubungi) {
                    listener.onHubungiCreditor(currentDebt);
                    return true;
                } else if (itemId == R.id.menuRiwayat) {
                    listener.onRiwayatPembayaran(currentDebt);
                    return true;
                } else if (itemId == R.id.menuBayar) {
                    listener.onBayar(currentDebt);
                    return true;
                } else if (itemId == R.id.menuHapus) {
                    listener.onHapus(currentDebt);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        // (Opsional) Jika Anda ingin menangani klik item card secara keseluruhan:
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Misal: buka detail hutang
                listener.onItemClicked(currentDebt);
            }
        });
    }

    @Override
    public int getItemCount() {
        return debtList.size();
    }

    // Metode untuk memperbarui data
    public void setDebtList(List<DebtWithCreditor> newDebtList) {
        this.debtList = newDebtList;
        notifyDataSetChanged();
    }

    public List<DebtWithCreditor> getDebtList() {
        return this.debtList;
    }

    // Interface listener
    public interface OnItemClickListener {
        // Klik pada item card
        void onItemClicked(DebtWithCreditor debt);

        // Klik "Hubungi Creditor"
        void onHubungiCreditor(DebtWithCreditor debt);

        // Klik "Riwayat Pembayaran"
        void onRiwayatPembayaran(DebtWithCreditor debt);

        // Klik "Bayar"
        void onBayar(DebtWithCreditor debt);

        // Klik "Hapus"
        void onHapus(DebtWithCreditor debt);
    }

    // Setter listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder
    class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTransactionId;
        TextView textViewHutangTransactionName;
        TextView textViewHutangDate;
        TextView textViewHutangTotal;
        TextView textViewHutangPaid;
        TextView textViewCashName;
        TextView textViewCreditorName;
        ImageView imageViewMore;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            // Hubungkan ke komponen di item_hutang.xml
            textViewTransactionId = itemView.findViewById(R.id.textViewTransactionId);
            textViewHutangTransactionName = itemView.findViewById(R.id.textViewSaleTransactionName);
            textViewHutangDate = itemView.findViewById(R.id.textViewSaleDate);
            textViewHutangTotal = itemView.findViewById(R.id.textViewHutangTotal);
            textViewHutangPaid = itemView.findViewById(R.id.textViewHutangPaid);

            textViewCreditorName = itemView.findViewById(R.id.textViewCreditorName);
            imageViewMore = itemView.findViewById(R.id.imageViewMore);
        }
    }
}
