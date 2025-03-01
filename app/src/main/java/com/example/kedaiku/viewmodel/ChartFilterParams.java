package com.example.kedaiku.viewmodel;

public class ChartFilterParams {
    public String option; // Contoh: "Bulan" atau "Tahun"
    public Long startDate;
    public Long endDate;

    public ChartFilterParams(String option, Long startDate, Long endDate) {
        this.option = option;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
