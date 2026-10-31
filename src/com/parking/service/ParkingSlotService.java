package com.parking.service;

import com.parking.dao.ParkingSlotDao;
import com.parking.model.ParkingSlot;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;

import java.util.List;

/**
 * Service class handling parking slot management business logic.
 * Sits cleanly between presentation controllers and the ParkingSlotDao.
 */
public class ParkingSlotService {
    private final ParkingSlotDao slotDao;

    public ParkingSlotService(ParkingSlotDao slotDao) {
        this.slotDao = slotDao;
    }

    public List<ParkingSlot> getAllSlots() {
        return slotDao.findAll();
    }

    public List<ParkingSlot> getOccupiedSlots() {
        return slotDao.findOccupiedSlots();
    }

    public List<ParkingSlot> getAvailableSlots() {
        return slotDao.findAvailableSlots();
    }

    public List<ParkingSlot> getSlotsByType(VehicleType type) {
        return slotDao.findByType(type);
    }

    public ParkingSlot findAvailableSlot(VehicleType type) {
        return slotDao.findAvailableSlotByType(type);
    }

    public ParkingSlot getSlotByNumber(int slotNumber) {
        return slotDao.findBySlotNumber(slotNumber);
    }

    public ParkingSlot getSlotByCode(String slotCode) {
        return slotDao.findBySlotCode(slotCode);
    }

    public int getTotalSlotsCount() {
        return slotDao.findAll().size();
    }

    public int getOccupiedCount() {
        return slotDao.findOccupiedSlots().size();
    }

    public int getAvailableCount() {
        return slotDao.findAvailableSlots().size();
    }

    public int getAvailableCountByType(VehicleType type) {
        int count = 0;
        for (ParkingSlot s : slotDao.findByType(type)) {
            if (s.isAvailable()) count++;
        }
        return count;
    }

    public int getOccupiedCountByType(VehicleType type) {
        int count = 0;
        for (ParkingSlot s : slotDao.findByType(type)) {
            if (s.isOccupied()) count++;
        }
        return count;
    }

    public int getTotalSlotsCount(VehicleType type) {
        return slotDao.findByType(type).size();
    }

    public synchronized void occupySlot(int slotNumber, Vehicle vehicle) {
        ParkingSlot slot = slotDao.findBySlotNumber(slotNumber);
        if (slot != null) {
            slot.assignVehicle(vehicle);
            slotDao.updateOccupancy(slotNumber, true, vehicle.getRegistrationNumber());
        }
    }

    public synchronized void releaseSlot(int slotNumber) {
        ParkingSlot slot = slotDao.findBySlotNumber(slotNumber);
        if (slot != null) {
            slot.vacate();
            slotDao.updateOccupancy(slotNumber, false, null);
        }
    }
}
