package com.ren.tutornearme.model;

public class Barangay {
    private String name;

    public Barangay() {}

    public Barangay(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Barangay{" +
                "name='" + name + '\'' +
                '}';
    }
}
