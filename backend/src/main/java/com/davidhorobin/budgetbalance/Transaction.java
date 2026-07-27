package com.davidhorobin.budgetbalance;

public class Transaction {

    private float value;
    private String vendor;

    public Transaction(float value, String vendor) {
        this.value = value;
        this.vendor = vendor;
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
