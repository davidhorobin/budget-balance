package com.davidhorobin.budgetbalance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class Transaction {

    @Null
    private String id;
    @Positive
    private float value;
    @NotEmpty
    private String vendor;

    public Transaction(String id, float value, String vendor) {
        this.id = id;
        this.value = value;
        this.vendor = vendor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
