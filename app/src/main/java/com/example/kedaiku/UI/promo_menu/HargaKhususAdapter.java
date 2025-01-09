package com.example.kedaiku.UI.promo_menu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.SpecialPriceWithProduct;

import java.lang.reflect.Member;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HargaKhususAdapter extends RecyclerView.Adapter<HargaKhususAdapter.HargaKhususViewHolder> {

    private List<SpecialPriceWithProduct> dataList;

    public List<CustomerGroup> getDataMember() {
        return dataMember;
    }

    public void setDataMember(List<CustomerGroup> dataMember) {
        this.dataMember = dataMember;
        notifyDataSetChanged();
    }

    private List<CustomerGroup> dataMember;
    private final OnItemClickListener listener;

    public List<SpecialPriceWithProduct> getSpecialPriceList() {
        return dataList;
    }

    public interface OnItemClickListener {
        void onUpdateClicked(SpecialPriceWithProduct specialPriceWithProduct);
        void onDeleteClicked(SpecialPrice specialPrice);
    }

    public HargaKhususAdapter(List<SpecialPriceWithProduct> dataList, List<CustomerGroup> dataMember, OnItemClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
        this.dataMember = dataMember;
    }

    public void setData(List<SpecialPriceWithProduct> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HargaKhususViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harga_khusus, parent, false);
        return new HargaKhususViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HargaKhususViewHolder holder, int position) {
        SpecialPriceWithProduct currentItemSpecialPriceWithProduct = dataList.get(position);
        SpecialPrice currentItemSpecialPrice = new SpecialPrice(

                currentItemSpecialPriceWithProduct.getName(),
                currentItemSpecialPriceWithProduct.getProduct_id(),
                currentItemSpecialPriceWithProduct.getGroup_id(),
                currentItemSpecialPriceWithProduct.getPrecent(),
                currentItemSpecialPriceWithProduct.getStatus()
        );
        currentItemSpecialPrice.set_id(currentItemSpecialPriceWithProduct.get_id());

        // Bind data
        holder.textViewProductName.setText(currentItemSpecialPriceWithProduct.getProduct_name());
        holder.textViewSpecialPriceName.setText("Nama Harga Khusus: " + currentItemSpecialPriceWithProduct.getName());
        holder.textViewHpp.setText("HPP: " + formatToRupiah(currentItemSpecialPriceWithProduct.getProduct_primary_price()));
        holder.textViewStatus.setText("Status: " + (currentItemSpecialPriceWithProduct.getStatus() == 1 ? "Aktif" : "Tidak Aktif"));
        holder.textViewNormalPrice.setText("Harga Normal: " + formatToRupiah(currentItemSpecialPriceWithProduct.getProduct_price()));
        holder.textViewDiscountPercent.setText("Persen Potongan: " + currentItemSpecialPriceWithProduct.getPrecent() + "%");
        holder.textViewSpecialPrice.setText("Harga Khusus: " + formatToRupiah((100-currentItemSpecialPriceWithProduct.getPrecent())/100*currentItemSpecialPriceWithProduct.getProduct_price()));
        holder.textViewMembership.setText("Membership : "+getGroupNameByid(currentItemSpecialPriceWithProduct.getGroup_id()));

        if (currentItemSpecialPriceWithProduct.getStatus() == 1) {
            holder.itemView.setBackgroundColor(Color.WHITE); // Background normal
        } else {
            holder.itemView.setBackgroundColor(Color.RED); // Background merah untuk tidak aktif
        }

        // Set item click listener to handle direct clicks
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onUpdateClicked(currentItemSpecialPriceWithProduct);
            }
        });

        // Set menu actions
        holder.imageViewMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.imageViewMenu);
            popupMenu.inflate(R.menu.menu_item_options);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_edit) {
                    if (listener != null) {
                        listener.onUpdateClicked(currentItemSpecialPriceWithProduct);
                    }
                    return true;

                } else if (itemId == R.id.menu_hapus) {
                    if (listener != null) {
                        listener.onDeleteClicked(currentItemSpecialPrice);
                    }
                    return true;
                }

                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    static class HargaKhususViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewSpecialPriceName;
        private final TextView textViewHpp;
        private final TextView textViewStatus;
        private final TextView textViewNormalPrice;
        private final TextView textViewDiscountPercent;
        private final TextView textViewSpecialPrice;
        private final TextView textViewMembership;
        private final ImageView imageViewMenu;

        public HargaKhususViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewSpecialPriceName = itemView.findViewById(R.id.textViewSpecialPriceName);
            textViewHpp = itemView.findViewById(R.id.textViewHpp);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewNormalPrice = itemView.findViewById(R.id.textViewNormalPrice);
            textViewDiscountPercent = itemView.findViewById(R.id.textViewDiscountPercentage);
            textViewSpecialPrice = itemView.findViewById(R.id.textViewSpecialPrice);
            textViewMembership = itemView.findViewById(R.id.textViewMembershipStatus);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }
    }

    private String formatToRupiah(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(value);
    }

    private long getGroupIdByName(String groupName) {
        for (CustomerGroup group : dataMember) {
            if (group.getName().equals(groupName)) {
                return group.getId();  // Return the groupId of the selected group
            } else if (groupName.equals("Seluruh Member")) {
                return 0;
            }
        }
        return -1;  // Return -1 if no matching group is found
    }


    private String getGroupNameByid(long id) {
        for (CustomerGroup group : dataMember) {
            if (group.getId()==id) {
                return group.getName();  // Return the groupId of the selected group
            } else if (id==0) {
                return "Seluruh Member";
            }
        }
        return "";  // Return -1 if no matching group is found
    }


}
