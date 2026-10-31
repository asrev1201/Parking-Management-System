package com.parking.dao;

import com.parking.model.ParkingSlot;
import com.parking.model.VehicleType;

import java.util.List;

/**
 * Data Access Object (DAO) interface for ParkingSlot persistence operations.
 * Fully abstracts SQL and underlying storage from service and presentation layers.
 */
public interface ParkingSlotDao {

    /**
     * Retrieves all parking slots in the facility.
     */
    List<ParkingSlot> findAll();

    /**
     * Finds a slot by its unique integer number.
     */
    ParkingSlot findBySlotNumber(int slotNumber);

    /**
     * Finds a slot by its alphanumeric code (e.g., 'BK-01', 'CR-05').
     */
    ParkingSlot findBySlotCode(String slotCode);

    /**
     * Retrieves all currently occupied slots.
     */
    List<ParkingSlot> findOccupiedSlots();

    /**
     * Retrieves all vacant / available slots.
     */
    List<ParkingSlot> findAvailableSlots();

    /**
     * Retrieves slots designated for a specific vehicle category.
     */
    List<ParkingSlot> findByType(VehicleType type);

    /**
     * Finds the first available slot designated for a specific vehicle category.
     */
    ParkingSlot findAvailableSlotByType(VehicleType type);

    /**
     * Updates occupancy status and assigned vehicle for a slot.
     */
    void updateOccupancy(int slotNumber, boolean isOccupied, String vehicleRegNumber);
}
