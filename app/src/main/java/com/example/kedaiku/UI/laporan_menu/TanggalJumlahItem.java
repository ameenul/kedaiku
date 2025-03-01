package com.example.kedaiku.UI.laporan_menu;

public class TanggalJumlahItem {
    private String tanggal;
    private String jumlah;

    public TanggalJumlahItem(String tanggal, String jumlah) {
        this.tanggal = tanggal;
        this.jumlah = jumlah;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJumlah() {
        return jumlah;
    }
}
