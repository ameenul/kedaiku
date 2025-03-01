package com.example.kedaiku.repository;

public interface OnTransactionCompleteListener {
    void onSuccess(boolean status);
    void onFailure(boolean status);
}