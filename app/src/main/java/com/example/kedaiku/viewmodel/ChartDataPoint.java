package com.example.kedaiku.viewmodel;

public class ChartDataPoint {
    private String label; // Misalnya "1", "2", ..., untuk hari atau "Jan", "Feb", ... untuk bulan
    private double value; // Total penjualan

    public ChartDataPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }
}
