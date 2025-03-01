package com.example.kedaiku.UI.pengeluaran_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.entites.ExpenseWithCash;
import com.example.kedaiku.helper.FormatHelper;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<ExpenseWithCash> expenseList;
    private Context context;
    private OnExpenseItemClickListener listener;

    public interface OnExpenseItemClickListener {
        void onDeleteExpense(ExpenseWithCash expense);
        void onUpdateExpense(ExpenseWithCash expense);
    }

    public ExpenseAdapter(Context context, List<ExpenseWithCash> expenseList, OnExpenseItemClickListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
    }

    public void setData(List<ExpenseWithCash> expenseList) {
        this.expenseList = expenseList;
        notifyDataSetChanged();
    }

    public List<ExpenseWithCash> getData() {
        return expenseList;
    }

    @NonNull
    @Override
    public ExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseAdapter.ViewHolder holder, int position) {
        ExpenseWithCash expense = expenseList.get(position);
        // Tampilkan tanggal dan ID (misalnya: "03/01/2025 - 15")
        String dateId = FormatHelper.getDescDate(expense.getExpense().getExpense_date())  + " - " + expense.getExpense().get_id();
        holder.tvExpenseDateId.setText(dateId);
        holder.tvExpenseDescription.setText(expense.getExpense().getExpense_name());
        holder.tvExpenseAmount.setText("Jumlah: Rp " + expense.getExpense().getExpense_amount());
        // Jika relasi cash tersedia, tampilkan nama kas; jika tidak, tampilkan tanda "-"
        holder.tvExpenseCashName.setText("Kas: " + (expense.getCash() != null ? expense.getCash().getCash_name() : "-"));

        // Tampilkan opsi update/hapus dengan PopupMenu pada ImageButton
        holder.btnExpenseOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnExpenseOptions);
            popup.inflate(R.menu.menu_item_options); // pastikan file res/menu/menu_item_options.xml sudah ada
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit) {
                    listener.onUpdateExpense(expense);
                    return true;
                } else if (id == R.id.menu_hapus) {
                    listener.onDeleteExpense(expense);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return expenseList != null ? expenseList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseDateId, tvExpenseDescription, tvExpenseAmount, tvExpenseCashName;
        ImageButton btnExpenseOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDateId = itemView.findViewById(R.id.tvExpenseDateId);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseCashName = itemView.findViewById(R.id.tvExpenseCashName);
            btnExpenseOptions = itemView.findViewById(R.id.btnExpenseOptions);
        }
    }
}
