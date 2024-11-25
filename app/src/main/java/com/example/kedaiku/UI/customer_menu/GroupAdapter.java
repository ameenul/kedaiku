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
import com.example.kedaiku.entites.CustomerGroup;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<CustomerGroup> groupList = new ArrayList<>();
    private OnItemClickListener listener;

    public GroupAdapter(Context context) {
        this.context = context;
    }

    public void setGroupList(List<CustomerGroup> groups) {
        this.groupList = groups;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onEditClicked(CustomerGroup group);
        void onDeleteClicked(CustomerGroup group);
        void onDetailClicked(CustomerGroup group);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        CustomerGroup group = groupList.get(position);
        holder.textViewGroupName.setText(group.getName());
        holder.textViewGroupId.setText("ID: " + group.getId());

        holder.imageViewOptions.setOnClickListener(v -> {
            // Tampilkan PopupMenu
            PopupMenu popupMenu = new PopupMenu(context, holder.imageViewOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_group_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit_group) {
                    listener.onEditClicked(group);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_group) {
                    listener.onDeleteClicked(group);
                    return true;
                } else if (item.getItemId() == R.id.action_view_group) {
                    listener.onDetailClicked(group);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.itemView.setOnClickListener(v -> {
            listener.onDetailClicked(group);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView textViewGroupName, textViewGroupId;
        ImageView imageViewOptions;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            textViewGroupId = itemView.findViewById(R.id.textViewGroupId);
            imageViewOptions = itemView.findViewById(R.id.imageViewGroupOptions);
        }
    }
}

