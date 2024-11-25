package com.example.kedaiku.UI.customer_menu;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<Customer> customerList = new ArrayList<>();
    private OnItemClickListener listener;

    public CustomerAdapter(Context context) {
        this.context = context;
    }

    public void setCustomerList(List<Customer> customers) {
        this.customerList = customers;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClicked(Customer customer);
        void onDeleteClicked(Customer customer);
        void onItemClicked(Customer customer);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.textViewName.setText(customer.getCustomer_name());
        holder.textViewPhone.setText(customer.getCustomer_phone());
        holder.textViewGroupId.setText("Group ID: " + customer.getCustomer_group_id());

        holder.imageViewOptions.setOnClickListener(v -> {
            // Tampilkan PopupMenu
            PopupMenu popupMenu = new PopupMenu(context, holder.imageViewOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_customer_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_customer) {
                    listener.onEditClicked(customer);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_customer) {
                    listener.onDeleteClicked(customer);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.itemView.setOnClickListener(v -> {
            listener.onItemClicked(customer);
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhone, textViewGroupId;
        ImageView imageViewOptions;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewCustomerName);
            textViewPhone = itemView.findViewById(R.id.textViewCustomerPhone);
            textViewGroupId = itemView.findViewById(R.id.textViewCustomerGroupId);
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
        }
    }
}

