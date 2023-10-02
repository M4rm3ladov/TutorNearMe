package com.ren.tutornearme.model;

public class Address {
    private String barangayName;

    public Address() {}

    public Address(String name) {
        this.barangayName = name;
    }

    public String getBarangayName() {
        return barangayName;
    }

    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
    }

    @Override
    public String toString() {
        return "Barangay{" +
                "name='" + barangayName + '\'' +
                '}';
    }
}
