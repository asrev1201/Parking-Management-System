package com.parking.dao;

import com.parking.data.DatabaseConnection;
import com.parking.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JDBC Implementation of ParkingTicketDao.
 * Executes clean parameterized queries to persist and retrieve parking tickets.
 */
public class ParkingTicketDaoImpl implements ParkingTicketDao {
    private static final String CSV_BACKUP_PATH = "data/parking_records.csv";
    private static final String CSV_HEADER = "TicketID,RegNumber,OwnerName,VehicleType,SlotNumber,SlotCode,EntryTime,ExitTime,DurationHours,HourlyRate,TotalFee,Status";

    private final List<ParkingTicket> ticketCache = new CopyOnWriteArrayList<>();

    public ParkingTicketDaoImpl() {
        loadInitialData();
    }

    private void loadInitialData() {
        boolean loadedFromDb = false;
        if (DatabaseConnection.testConnection()) {
            loadedFromDb = loadFromDatabase();
        }

        if (!loadedFromDb || ticketCache.isEmpty()) {
            loadFromCsv();
        }
    }

    private boolean loadFromDatabase() {
        String sql = "SELECT ticket_id, reg_number, owner_name, vehicle_type, slot_number, slot_code, " +
                "entry_time, exit_time, duration_hours, hourly_rate, total_fee, status " +
                "FROM parking_tickets ORDER BY entry_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ticketId = rs.getString("ticket_id");
                String regNumber = rs.getString("reg_number");
                String ownerName = rs.getString("owner_name");
                String typeStr = rs.getString("vehicle_type");
                int slotNumber = rs.getInt("slot_number");
                String slotCode = rs.getString("slot_code");
                Timestamp entryTs = rs.getTimestamp("entry_time");
                Timestamp exitTs = rs.getTimestamp("exit_time");
                long duration = rs.getLong("duration_hours");
                double rate = rs.getDouble("hourly_rate");
                double fee = rs.getDouble("total_fee");
                String status = rs.getString("status");

                VehicleType vt = VehicleType.fromString(typeStr);
                if (vt == null) vt = VehicleType.CAR;

                Vehicle v;
                switch (vt) {
                    case BIKE: v = new Bike(regNumber, ownerName); break;
                    case BUS:  v = new Bus(regNumber, ownerName);  break;
                    case CAR:
                    default:   v = new Car(regNumber, ownerName);  break;
                }

                LocalDateTime entry = entryTs != null ? entryTs.toLocalDateTime() : LocalDateTime.now();
                LocalDateTime exit = exitTs != null ? exitTs.toLocalDateTime() : null;

                ParkingTicket ticket = new ParkingTicket(ticketId, v, slotNumber, slotCode,
                        entry, exit, duration, rate, fee, status);
                ticketCache.add(ticket);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Notice reading tickets from database: " + e.getMessage());
            return false;
        }
    }

    private void loadFromCsv() {
        File file = new File(CSV_BACKUP_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 12) continue;

                String ticketId = p[0].trim();
                String reg = p[1].trim();
                String owner = p[2].trim();
                VehicleType vt = VehicleType.fromString(p[3].trim());
                if (vt == null) vt = VehicleType.CAR;
                int slotNum = Integer.parseInt(p[4].trim());
                String slotCode = p[5].trim();
                LocalDateTime entry = LocalDateTime.parse(p[6].trim(), ParkingTicket.DATE_TIME_FORMATTER);
                LocalDateTime exit = (p[7].isEmpty() || p[7].equals("null")) ? null :
                        LocalDateTime.parse(p[7].trim(), ParkingTicket.DATE_TIME_FORMATTER);
                long duration = p[8].isEmpty() ? 0 : Long.parseLong(p[8].trim());
                double rate = p[9].isEmpty() ? 0.0 : Double.parseDouble(p[9].trim());
                double fee = p[10].isEmpty() ? 0.0 : Double.parseDouble(p[10].trim());
                String status = p[11].trim();

                Vehicle v;
                switch (vt) {
                    case BIKE: v = new Bike(reg, owner); break;
                    case BUS:  v = new Bus(reg, owner);  break;
                    case CAR:
                    default:   v = new Car(reg, owner);  break;
                }

                ParkingTicket t = new ParkingTicket(ticketId, v, slotNum, slotCode, entry, exit, duration, rate, fee, status);
                ticketCache.add(t);
            }
        } catch (Exception ignored) {}
    }

    private void syncToCsv() {
        File file = new File(CSV_BACKUP_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER);
            for (ParkingTicket t : ticketCache) {
                String entryStr = t.getEntryTime() != null ? t.getEntryTime().format(ParkingTicket.DATE_TIME_FORMATTER) : "";
                String exitStr = t.getExitTime() != null ? t.getExitTime().format(ParkingTicket.DATE_TIME_FORMATTER) : "";
                String safeOwner = t.getVehicle().getOwnerName().replace(",", " ");
                String safeReg = t.getVehicle().getRegistrationNumber().replace(",", " ");

                String line = String.format("%s,%s,%s,%s,%d,%s,%s,%s,%d,%.2f,%.2f,%s",
                        t.getTicketId(), safeReg, safeOwner, t.getVehicle().getVehicleType().name(),
                        t.getSlotNumber(), t.getSlotCode(), entryStr, exitStr,
                        t.getDurationHours(), t.getHourlyRate(), t.getTotalFee(), t.getStatus());
                writer.println(line);
            }
        } catch (IOException ignored) {}
    }

    @Override
    public void save(ParkingTicket ticket) {
        ticketCache.add(ticket);
        syncToCsv();

        if (DatabaseConnection.testConnection()) {
            String sql = "INSERT INTO parking_tickets (ticket_id, reg_number, owner_name, vehicle_type, " +
                    "slot_number, slot_code, entry_time, exit_time, duration_hours, hourly_rate, total_fee, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, ticket.getTicketId());
                ps.setString(2, ticket.getVehicle().getRegistrationNumber());
                ps.setString(3, ticket.getVehicle().getOwnerName());
                ps.setString(4, ticket.getVehicle().getVehicleType().name());
                ps.setInt(5, ticket.getSlotNumber());
                ps.setString(6, ticket.getSlotCode());
                ps.setTimestamp(7, Timestamp.valueOf(ticket.getEntryTime()));
                if (ticket.getExitTime() != null) {
                    ps.setTimestamp(8, Timestamp.valueOf(ticket.getExitTime()));
                } else {
                    ps.setNull(8, Types.TIMESTAMP);
                }
                ps.setLong(9, ticket.getDurationHours());
                ps.setDouble(10, ticket.getHourlyRate());
                ps.setDouble(11, ticket.getTotalFee());
                ps.setString(12, ticket.getStatus());
                ps.executeUpdate();

            } catch (SQLException e) {
                System.err.println("DAO insert ticket error: " + e.getMessage());
            }
        }
    }

    @Override
    public void update(ParkingTicket ticket) {
        syncToCsv();

        if (DatabaseConnection.testConnection()) {
            String sql = "UPDATE parking_tickets SET exit_time = ?, duration_hours = ?, total_fee = ?, status = ? WHERE ticket_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setTimestamp(1, Timestamp.valueOf(ticket.getExitTime()));
                ps.setLong(2, ticket.getDurationHours());
                ps.setDouble(3, ticket.getTotalFee());
                ps.setString(4, ticket.getStatus());
                ps.setString(5, ticket.getTicketId());
                ps.executeUpdate();

            } catch (SQLException e) {
                System.err.println("DAO update ticket error: " + e.getMessage());
            }
        }
    }

    @Override
    public ParkingTicket findById(String ticketId) {
        if (ticketId == null) return null;
        for (ParkingTicket t : ticketCache) {
            if (t.getTicketId().equalsIgnoreCase(ticketId.trim())) {
                return t;
            }
        }
        return null;
    }

    @Override
    public ParkingTicket findActiveByRegNumber(String regNumber) {
        if (regNumber == null) return null;
        String clean = regNumber.trim().toUpperCase();
        for (ParkingTicket t : ticketCache) {
            if (t.isActive() && t.getVehicle().getRegistrationNumber().equalsIgnoreCase(clean)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public ParkingTicket findActiveByQuery(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String clean = query.trim().toUpperCase();
        for (ParkingTicket t : ticketCache) {
            if (t.isActive()) {
                if (t.getTicketId().equalsIgnoreCase(clean) || 
                    t.getVehicle().getRegistrationNumber().equalsIgnoreCase(clean)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public ParkingTicket findCompletedByQuery(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String clean = query.trim().toUpperCase();
        for (ParkingTicket t : ticketCache) {
            if (!t.isActive()) {
                if (t.getTicketId().equalsIgnoreCase(clean) || 
                    t.getVehicle().getRegistrationNumber().equalsIgnoreCase(clean)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public List<ParkingTicket> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(ticketCache));
    }

    @Override
    public List<ParkingTicket> findActiveTickets() {
        List<ParkingTicket> list = new ArrayList<>();
        for (ParkingTicket t : ticketCache) {
            if (t.isActive()) list.add(t);
        }
        return list;
    }

    @Override
    public List<ParkingTicket> findCompletedTickets() {
        List<ParkingTicket> list = new ArrayList<>();
        for (ParkingTicket t : ticketCache) {
            if (!t.isActive()) list.add(t);
        }
        return list;
    }

    @Override
    public void delete(String ticketId) {
        if (ticketId == null) return;
        ticketCache.removeIf(t -> t.getTicketId().equalsIgnoreCase(ticketId.trim()));
        syncToCsv();

        if (DatabaseConnection.testConnection()) {
            String sql = "DELETE FROM parking_tickets WHERE ticket_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ticketId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("DAO delete ticket error: " + e.getMessage());
            }
        }
    }
}
