package com.example.kedaiku.UI.utang_menu;


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
import com.example.kedaiku.entites.Creditor;

import java.util.ArrayList;
import java.util.List;

public class CreditorAdapter extends RecyclerView.Adapter<CreditorAdapter.CreditorViewHolder> {

    private Context context;
    private List<Creditor> creditorList = new ArrayList<>();
    private OnItemClickListener listener;

    public CreditorAdapter(Context context) {
        this.context = context;
    }

    public void setCreditorList(List<Creditor> creditors) {
        this.creditorList = creditors;
        notifyDataSetChanged();
    }

    public List<Creditor> getCreditorList() {
        return creditorList; // creditorList adalah data internal adapter
    }


    public interface OnItemClickListener {
        void onEditClicked(Creditor creditor);
        void onDeleteClicked(Creditor creditor);
        void onItemClicked(Creditor creditor);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CreditorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_creditor, parent, false);
        return new CreditorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditorViewHolder holder, int position) {
        Creditor creditor = creditorList.get(position);
        holder.textViewName.setText(creditor.getCreditor_name());
        holder.textViewPhone.setText(creditor.getCreditor_phone());
        holder.imageViewOptions.setOnClickListener(v -> {
            // Tampilkan PopupMenu
            PopupMenu popupMenu = new PopupMenu(context, holder.imageViewOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_creditor_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_creditor) {
                    listener.onEditClicked(creditor);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_creditor) {
                    listener.onDeleteClicked(creditor);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.itemView.setOnClickListener(v -> {
            listener.onItemClicked(creditor);
        });
    }

    @Override
    public int getItemCount() {
        return creditorList.size();
    }

    class CreditorViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhone, textViewGroupId;
        ImageView imageViewOptions;

        public CreditorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewCreditorName);
            textViewPhone = itemView.findViewById(R.id.textViewCreditorPhone);
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
        }
    }
}

