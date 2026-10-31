package com.parking.model;

/**
 * Enum representing supported vehicle types and their standard hourly rates.
 */
public enum VehicleType {
    BIKE("Bike", 20.0, "Two-Wheeler"),
    CAR("Car", 50.0, "Four-Wheeler"),
    BUS("Bus", 100.0, "Heavy Vehicle");

    private final String displayName;
    private final double defaultHourlyRate;
    private final String category;

    VehicleType(String displayName, double defaultHourlyRate, String category) {
        this.displayName = displayName;
        this.defaultHourlyRate = defaultHourlyRate;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public String getCategory() {
        return category;
    }

    public static VehicleType fromString(String text) {
        if (text == null) return null;
        for (VehicleType vt : VehicleType.values()) {
            if (vt.name().equalsIgnoreCase(text.trim()) || 
                vt.displayName.equalsIgnoreCase(text.trim())) {
                return vt;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
