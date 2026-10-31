package com.parking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a parking ticket issued when a vehicle enters the facility.
 * Records timestamps, assigned slot, billing information, and status.
 */
public class ParkingTicket {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String ticketId;
    private final Vehicle vehicle;
    private final int slotNumber;
    private final String slotCode;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private long durationHours;
    private double hourlyRate;
    private double totalFee;
    private String status;

    /**
     * Constructor used when a new ticket is generated at vehicle entry.
     */
    public ParkingTicket(String ticketId, Vehicle vehicle, int slotNumber, String slotCode, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.slotNumber = slotNumber;
        this.slotCode = slotCode;
        this.entryTime = entryTime != null ? entryTime : LocalDateTime.now();
        this.exitTime = null;
        this.durationHours = 0;
        this.hourlyRate = vehicle.getHourlyRate();
        this.totalFee = 0.0;
        this.status = STATUS_ACTIVE;
    }

    /**
     * Full constructor used when restoring persisted records from storage.
     */
    public ParkingTicket(String ticketId, Vehicle vehicle, int slotNumber, String slotCode, 
                         LocalDateTime entryTime, LocalDateTime exitTime, 
                         long durationHours, double hourlyRate, double totalFee, String status) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.slotNumber = slotNumber;
        this.slotCode = slotCode;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.hourlyRate = hourlyRate;
        this.totalFee = totalFee;
        this.status = status;
    }

    /**
     * Completes the ticket on vehicle exit.
     */
    public void completeTicket(LocalDateTime exitTime, long durationHours, double totalFee) {
        if (!isActive()) {
            throw new IllegalStateException("Ticket " + ticketId + " has already been settled and completed.");
        }
        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.totalFee = totalFee;
        this.status = STATUS_COMPLETED;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    // Getters
    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getDurationHours() {
        return durationHours;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public String getStatus() {
        return status;
    }

    // Formatted helper methods for UI
    public String getFormattedEntryTime() {
        return entryTime != null ? entryTime.format(DATE_TIME_FORMATTER) : "N/A";
    }

    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(DATE_TIME_FORMATTER) : "-- (Currently Parked)";
    }

    public String getFormattedDuration() {
        if (isActive()) {
            return "Active Session";
        }
        return durationHours + (durationHours == 1 ? " hr" : " hrs");
    }

    public String getFormattedFee() {
        if (isActive()) {
            return "Pending";
        }
        return String.format("Rs. %.2f", totalFee);
    }

    @Override
    public String toString() {
        return "Ticket #" + ticketId + " [" + vehicle.getRegistrationNumber() + "] Slot: " + slotCode 
                + " | Status: " + status + " | Fee: " + getFormattedFee();
    }
}
