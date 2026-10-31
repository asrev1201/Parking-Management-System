package com.parking.model;

/**
 * Subclass representing a Heavy Vehicle / Bus.
 * Demonstrates Inheritance and Polymorphic implementation of hourly rates.
 */
public class Bus extends Vehicle {
    private static final double BUS_RATE = 100.0;

    public Bus(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.BUS);
    }

    @Override
    public double getHourlyRate() {
        return BUS_RATE;
    }

    @Override
    public String getVehicleCategoryDescription() {
        return "Heavy Vehicle / Transport Bus / Truck";
    }
}
