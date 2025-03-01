package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.repository.CreditorRepository;

import java.util.List;

public class CreditorViewModel extends AndroidViewModel {

    private CreditorRepository repository;
    private LiveData<List<Creditor>> allCreditors;

    private MutableLiveData<String> searchQuery = new MutableLiveData<>();

    private LiveData<List<Creditor>> creditors;

    private final MutableLiveData<Creditor> selectedCreditor = new MutableLiveData<>();

    public CreditorViewModel(@NonNull Application application) {
        super(application);
        repository = new CreditorRepository(application);
        allCreditors = repository.getAllCreditors();

        creditors = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return allCreditors;
            } else {
                return repository.searchCreditors(query);
            }
        });
    }

    public LiveData<List<Creditor>> getCreditors() {
        return creditors;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insert(Creditor creditor) {
        repository.insert(creditor);
    }

    public void update(Creditor creditor) {
        repository.update(creditor);
    }

    public void delete(Creditor creditor) {
        repository.delete(creditor);
    }

    public LiveData<Creditor> getCreditorById(long creditorId) {
        return repository.getCreditorById(creditorId);
    }

     public LiveData<List<Creditor>> getAllCreditors() {
        return repository.getAllCreditors();
    }

    public void setSelectedCreditor(Creditor creditor) {
        selectedCreditor.setValue(creditor);
    }

    public MutableLiveData<Creditor> getSelectedCreditor() {
        return selectedCreditor;
    }

}
