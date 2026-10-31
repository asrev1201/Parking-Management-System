package com.parking.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initializes database schemas and required default data.
 * Keeps table DDL and structural initialization completely isolated
 * from the presentation and DAO layers.
 */
public class DatabaseInitializer {

    private static final String CREATE_SLOTS_TABLE = 
            "CREATE TABLE IF NOT EXISTS parking_slots (" +
            "slot_number INT NOT NULL PRIMARY KEY, " +
            "slot_code VARCHAR(20) NOT NULL UNIQUE, " +
            "vehicle_type VARCHAR(20) NOT NULL, " +
            "is_occupied BOOLEAN NOT NULL DEFAULT FALSE, " +
            "current_reg_number VARCHAR(50) DEFAULT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

    private static final String CREATE_TICKETS_TABLE = 
            "CREATE TABLE IF NOT EXISTS parking_tickets (" +
            "ticket_id VARCHAR(50) NOT NULL PRIMARY KEY, " +
            "reg_number VARCHAR(50) NOT NULL, " +
            "owner_name VARCHAR(100) NOT NULL, " +
            "vehicle_type VARCHAR(20) NOT NULL, " +
            "slot_number INT NOT NULL, " +
            "slot_code VARCHAR(20) NOT NULL, " +
            "entry_time DATETIME NOT NULL, " +
            "exit_time DATETIME DEFAULT NULL, " +
            "duration_hours BIGINT NOT NULL DEFAULT 0, " +
            "hourly_rate DECIMAL(10, 2) NOT NULL, " +
            "total_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00, " +
            "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

    /**
     * Initializes tables and seeds default parking bays if necessary.
     */
    public static void initialize() {
        if (!DatabaseConnection.testConnection()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_SLOTS_TABLE);
            stmt.execute(CREATE_TICKETS_TABLE);

            // Check if slots table has records
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM parking_slots")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDefaultBays(conn);
                }
            }

        } catch (Exception e) {
            System.err.println("Database initialization note: " + e.getMessage());
        }
    }

    private static void seedDefaultBays(Connection conn) throws Exception {
        String insertSql = "INSERT INTO parking_slots (slot_number, slot_code, vehicle_type, is_occupied) VALUES (?, ?, ?, FALSE)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            // 10 Bike slots
            for (int i = 1; i <= 10; i++) {
                ps.setInt(1, i);
                ps.setString(2, String.format("BK-%02d", i));
                ps.setString(3, "BIKE");
                ps.addBatch();
            }
            // 15 Car slots
            for (int i = 1; i <= 15; i++) {
                int slotNum = 10 + i;
                ps.setInt(1, slotNum);
                ps.setString(2, String.format("CR-%02d", i));
                ps.setString(3, "CAR");
                ps.addBatch();
            }
            // 5 Bus slots
            for (int i = 1; i <= 5; i++) {
                int slotNum = 25 + i;
                ps.setInt(1, slotNum);
                ps.setString(2, String.format("BS-%02d", i));
                ps.setString(3, "BUS");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
