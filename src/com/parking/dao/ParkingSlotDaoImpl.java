package com.parking.dao;

import com.parking.data.DatabaseConnection;
import com.parking.model.ParkingSlot;
import com.parking.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC Implementation of ParkingSlotDao.
 * Executes parameterized SQL queries using PreparedStatement.
 * Completely isolates database operations from services and UI.
 */
public class ParkingSlotDaoImpl implements ParkingSlotDao {

    // In-memory slot cache to ensure continuous offline resilience
    private final Map<Integer, ParkingSlot> slotCache = new LinkedHashMap<>();

    public ParkingSlotDaoImpl() {
        initializeDefaultSlots();
        loadFromDatabase();
    }

    private void initializeDefaultSlots() {
        int slotNumber = 1;
        // 10 Bikes
        for (int i = 1; i <= 10; i++) {
            String code = String.format("BK-%02d", i);
            slotCache.put(slotNumber, new ParkingSlot(slotNumber++, code, VehicleType.BIKE));
        }
        // 15 Cars
        for (int i = 1; i <= 15; i++) {
            String code = String.format("CR-%02d", i);
            slotCache.put(slotNumber, new ParkingSlot(slotNumber++, code, VehicleType.CAR));
        }
        // 5 Buses
        for (int i = 1; i <= 5; i++) {
            String code = String.format("BS-%02d", i);
            slotCache.put(slotNumber, new ParkingSlot(slotNumber++, code, VehicleType.BUS));
        }
    }

    private void loadFromDatabase() {
        if (!DatabaseConnection.testConnection()) {
            return;
        }

        String sql = "SELECT slot_number, slot_code, vehicle_type, is_occupied FROM parking_slots ORDER BY slot_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int num = rs.getInt("slot_number");
                String code = rs.getString("slot_code");
                String typeStr = rs.getString("vehicle_type");
                VehicleType vt = VehicleType.fromString(typeStr);
                if (vt == null) vt = VehicleType.CAR;

                // Sync cache
                if (!slotCache.containsKey(num)) {
                    slotCache.put(num, new ParkingSlot(num, code, vt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Notice reading parking slots: " + e.getMessage());
        }
    }

    @Override
    public List<ParkingSlot> findAll() {
        return new ArrayList<>(slotCache.values());
    }

    @Override
    public ParkingSlot findBySlotNumber(int slotNumber) {
        return slotCache.get(slotNumber);
    }

    @Override
    public ParkingSlot findBySlotCode(String slotCode) {
        if (slotCode == null) return null;
        for (ParkingSlot s : slotCache.values()) {
            if (s.getSlotCode().equalsIgnoreCase(slotCode.trim())) {
                return s;
            }
        }
        return null;
    }

    @Override
    public List<ParkingSlot> findOccupiedSlots() {
        List<ParkingSlot> list = new ArrayList<>();
        for (ParkingSlot s : slotCache.values()) {
            if (s.isOccupied()) {
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public List<ParkingSlot> findAvailableSlots() {
        List<ParkingSlot> list = new ArrayList<>();
        for (ParkingSlot s : slotCache.values()) {
            if (s.isAvailable()) {
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public List<ParkingSlot> findByType(VehicleType type) {
        List<ParkingSlot> list = new ArrayList<>();
        for (ParkingSlot s : slotCache.values()) {
            if (s.getDesignatedType() == type) {
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public ParkingSlot findAvailableSlotByType(VehicleType type) {
        for (ParkingSlot s : slotCache.values()) {
            if (s.getDesignatedType() == type && s.isAvailable()) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void updateOccupancy(int slotNumber, boolean isOccupied, String vehicleRegNumber) {
        // 1. Update in-memory state
        ParkingSlot slot = slotCache.get(slotNumber);
        if (slot != null && !isOccupied) {
            slot.vacate();
        }

        // 2. Update database via JDBC PreparedStatement
        if (DatabaseConnection.testConnection()) {
            String sql = "UPDATE parking_slots SET is_occupied = ?, current_reg_number = ? WHERE slot_number = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, isOccupied);
                if (vehicleRegNumber != null && isOccupied) {
                    ps.setString(2, vehicleRegNumber);
                } else {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                }
                ps.setInt(3, slotNumber);
                ps.executeUpdate();

            } catch (SQLException e) {
                System.err.println("DAO update slot error: " + e.getMessage());
            }
        }
    }
}
