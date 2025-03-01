package com.example.kedaiku.UI.laporan_menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.kedaiku.UI.laporan_menu.tab_labarugi.LabaRugiFragment;
import com.example.kedaiku.UI.laporan_menu.tab_pengeluaran.PengeluaranFragment;
import com.example.kedaiku.UI.laporan_menu.tab_penjualan.PenjualanFragment;

public class LaporanPagerAdapter extends FragmentStateAdapter {

    public LaporanPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PenjualanFragment();
            case 1:
                return new PengeluaranFragment();
            case 2:
                return new LabaRugiFragment();
            case 3:
                return new LaporanUmumFragment();
            default:
                return new PenjualanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
