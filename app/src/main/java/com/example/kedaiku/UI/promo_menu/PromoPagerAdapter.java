package com.example.kedaiku.UI.promo_menu;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PromoPagerAdapter extends FragmentStateAdapter {

    public PromoPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HargaGrosirFragment();
            case 1:
                return new HargaKhususFragment();
            default:
                return new HargaGrosirFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Dua tab: Harga Grosir dan Harga Khusus
    }
}

