package com.parking.model;

/**
 * Abstract base class demonstrating Abstraction and Encapsulation.
 * Represents general vehicle properties and contracts for subclasses.
 */
public abstract class Vehicle {
    private String registrationNumber;
    private String ownerName;
    private VehicleType vehicleType;

    /**
     * Constructor for Vehicle.
     * @param registrationNumber Vehicle plate/registration identifier
     * @param ownerName Name of vehicle owner
     * @param vehicleType Type category (BIKE, CAR, BUS)
     */
    public Vehicle(String registrationNumber, String ownerName, VehicleType vehicleType) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type must be specified.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.ownerName = ownerName.trim();
        this.vehicleType = vehicleType;
    }

    /**
     * Abstract method to retrieve vehicle-specific hourly parking rate.
     * Demonstrates Polymorphism across Bike, Car, and Bus subclasses.
     */
    public abstract double getHourlyRate();

    /**
     * Abstract method to retrieve vehicle category details.
     */
    public abstract String getVehicleCategoryDescription();

    // Encapsulated getters and setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        this.ownerName = ownerName.trim();
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    @Override
    public String toString() {
        return vehicleType.getDisplayName() + " [" + registrationNumber + "] Owner: " + ownerName;
    }
}
