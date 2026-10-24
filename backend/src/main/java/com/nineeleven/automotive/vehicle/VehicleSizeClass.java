package com.nineeleven.automotive.vehicle;

public enum VehicleSizeClass {
    SMALL("Small"), MEDIUM("Medium"), LARGE("Large"), SUV("SUV");

    private final String label;
    VehicleSizeClass(String label) { this.label = label; }
    public String label() { return label; }
}
