package com.parking.model;

/**
 * Subclass representing a Four-Wheeler / Car.
 * Demonstrates Inheritance and Polymorphic implementation of hourly rates.
 */
public class Car extends Vehicle {
    private static final double CAR_RATE = 50.0;

    public Car(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.CAR);
    }

    @Override
    public double getHourlyRate() {
        return CAR_RATE;
    }

    @Override
    public String getVehicleCategoryDescription() {
        return "Four-Wheeler / Hatchback / Sedan / SUV";
    }
}
