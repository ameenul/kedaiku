package com.example.kedaiku.UI.penjualan_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.FormatHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> implements Filterable {

    private Context context;
    private List<SaleWithDetails> salesList;
    private List<SaleWithDetails> salesListFull;
    private OnItemClickListener onItemClickListener;



    public SalesAdapter(Context context, List<SaleWithDetails> salesList, OnItemClickListener listener) {
        this.context = context;
        this.salesList = salesList;
        this.onItemClickListener = listener;
    }
    public void setSalesWithDetails(List<SaleWithDetails> salesList) {
        this.salesList = salesList;
        this.salesListFull = new ArrayList<>(salesList);
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        SaleWithDetails saleWithDetails = salesList.get(position);

        holder.textViewSaleId.setText(String.valueOf(saleWithDetails.getSaleId()));
        holder.textViewSaleTransactionName.setText(saleWithDetails.getSaleTransactionName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(saleWithDetails.getSaleDate()));
        holder.textViewSaleDate.setText(formattedDate);

        holder.textViewSaleTotal.setText(FormatHelper.formatCurrency(saleWithDetails.getSaleTotal()));
        holder.textViewSalePaid.setText(FormatHelper.formatCurrency(saleWithDetails.getSalePaid()));
        holder.textViewSalePaymentType.setText(saleWithDetails.getSale().getSale_payment_type()==1?"Cash":"Piutang");
       holder.textViewCustomerName.setText(saleWithDetails.getCustomerName());
        holder.imageViewMore.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, v);
            popup.inflate(R.menu.menu_item_options); // menu_item_options.xml

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.menu_edit) {
                    // Panggil callback 'onEditClicked'
                    if (onItemClickListener != null) {
                        onItemClickListener.onEditClicked(saleWithDetails);
                    }
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_hapus) {
                    // Panggil callback 'onDeleteClicked'
                    if (onItemClickListener != null) {
                        onItemClickListener.onDeleteClicked(saleWithDetails);
                    }
                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });


        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClicked(saleWithDetails);
            }
        });

    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    @Override
    public Filter getFilter() {
        return salesFilter;
    }

    private Filter salesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SaleWithDetails> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(salesListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (SaleWithDetails sale : salesListFull) {
                    if (sale.getSaleTransactionName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(sale);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            salesList.clear();
            salesList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class SalesViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSaleId, textViewSaleTransactionName, textViewSaleDate,
                textViewSaleTotal, textViewSalePaid, textViewSalePaymentType, textViewCustomerName;

        ImageView imageViewMore;

        public SalesViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSaleId = itemView.findViewById(R.id.textViewSaleId);
            textViewSaleTransactionName = itemView.findViewById(R.id.textViewSaleTransactionName);
            textViewSaleDate = itemView.findViewById(R.id.textViewSaleDate);
            textViewSaleTotal = itemView.findViewById(R.id.textViewSaleTotal);
            textViewSalePaid = itemView.findViewById(R.id.textViewSalePaid);
            textViewSalePaymentType = itemView.findViewById(R.id.textViewSalePaymentType);
            textViewCustomerName = itemView.findViewById(R.id.textViewCustomerName);

            imageViewMore = itemView.findViewById(R.id.imageViewMore);
        }
    }

    // Callback interface: memudahkan Activity menangani event
    public interface OnItemClickListener {
        void onEditClicked(SaleWithDetails saleWithDetails);
        void onDeleteClicked(SaleWithDetails saleWithDetails);
        void onItemClicked(SaleWithDetails saleWithDetails); // Tambahkan ini
    }

    public List<SaleWithDetails> getSalesList() {
        return salesList;
    }



}
