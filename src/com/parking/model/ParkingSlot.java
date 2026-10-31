package com.parking.model;

/**
 * Represents an individual parking slot in the parking facility.
 * Enforces encapsulation and tracks slot availability and assigned vehicles.
 */
public class ParkingSlot {
    private final int slotNumber;
    private final String slotCode;
    private final VehicleType designatedType;
    private boolean occupied;
    private Vehicle currentVehicle;

    public ParkingSlot(int slotNumber, String slotCode, VehicleType designatedType) {
        this.slotNumber = slotNumber;
        this.slotCode = slotCode;
        this.designatedType = designatedType;
        this.occupied = false;
        this.currentVehicle = null;
    }

    public boolean isAvailable() {
        return !occupied && currentVehicle == null;
    }

    public synchronized void assignVehicle(Vehicle vehicle) {
        if (!isAvailable()) {
            throw new IllegalStateException("Slot " + slotCode + " (#" + slotNumber + ") is already occupied.");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Cannot assign a null vehicle to slot.");
        }
        if (vehicle.getVehicleType() != designatedType) {
            throw new IllegalArgumentException("Slot " + slotCode + " is reserved for " + designatedType 
                    + ", but attempted to park " + vehicle.getVehicleType());
        }
        this.currentVehicle = vehicle;
        this.occupied = true;
    }

    public synchronized Vehicle vacate() {
        Vehicle vacated = this.currentVehicle;
        this.currentVehicle = null;
        this.occupied = false;
        return vacated;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public VehicleType getDesignatedType() {
        return designatedType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    @Override
    public String toString() {
        return "Slot " + slotCode + " (#" + slotNumber + ") [" + designatedType + "] - " + 
               (occupied ? "OCCUPIED by " + currentVehicle.getRegistrationNumber() : "AVAILABLE");
    }
}
