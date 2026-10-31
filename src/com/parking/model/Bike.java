package com.parking.model;

/**
 * Subclass representing a Two-Wheeler / Bike.
 * Demonstrates Inheritance and Polymorphic implementation of hourly rates.
 */
public class Bike extends Vehicle {
    private static final double BIKE_RATE = 20.0;

    public Bike(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.BIKE);
    }

    @Override
    public double getHourlyRate() {
        return BIKE_RATE;
    }

    @Override
    public String getVehicleCategoryDescription() {
        return "Two-Wheeler / Motorcycle / Scooter";
    }
}
