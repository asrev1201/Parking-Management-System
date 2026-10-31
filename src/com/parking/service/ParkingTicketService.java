package com.parking.service;

import com.parking.dao.ParkingTicketDao;
import com.parking.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class handling parking ticket issuance, exit fee settlement,
 * and historical session queries. Coordinates between ParkingTicketDao
 * and ParkingSlotService.
 */
public class ParkingTicketService {
    private final ParkingTicketDao ticketDao;
    private final ParkingSlotService slotService;
    private final ParkingFeeCalculator feeCalculator;
    private int ticketCounter = 1000;

    public ParkingTicketService(ParkingTicketDao ticketDao, ParkingSlotService slotService, ParkingFeeCalculator feeCalculator) {
        this.ticketDao = ticketDao;
        this.slotService = slotService;
        this.feeCalculator = feeCalculator;
        syncTicketCounter();
    }

    private void syncTicketCounter() {
        Pattern pattern = Pattern.compile("TKT-(\\d+)");
        for (ParkingTicket t : ticketDao.findAll()) {
            Matcher m = pattern.matcher(t.getTicketId());
            if (m.find()) {
                try {
                    int num = Integer.parseInt(m.group(1));
                    if (num >= ticketCounter) {
                        ticketCounter = num + 1;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /**
     * Issues a new parking ticket for an incoming vehicle.
     */
    public synchronized ParkingTicket issueTicket(String regNumber, String ownerName, VehicleType vehicleType) {
        if (regNumber == null || regNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration Number is required.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner Name is required.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Please select a valid Vehicle Type.");
        }

        final String cleanReg = regNumber.trim().toUpperCase();
        final String cleanOwner = ownerName.trim();

        // Prevent duplicate active registration
        if (isVehicleParked(cleanReg)) {
            throw new IllegalStateException("Vehicle with Registration '" + cleanReg + 
                    "' is already currently parked in the lot!");
        }

        // Check slot availability
        ParkingSlot availableSlot = slotService.findAvailableSlot(vehicleType);
        if (availableSlot == null) {
            throw new IllegalStateException("No available parking slots for " + 
                    vehicleType.getDisplayName() + "! Capacity reached (" + 
                    slotService.getTotalSlotsCount(vehicleType) + " slots).");
        }

        // Polymorphic vehicle creation
        Vehicle vehicle;
        switch (vehicleType) {
            case BIKE: vehicle = new Bike(cleanReg, cleanOwner); break;
            case BUS:  vehicle = new Bus(cleanReg, cleanOwner);  break;
            case CAR:
            default:   vehicle = new Car(cleanReg, cleanOwner);  break;
        }

        // Occupy the physical slot
        slotService.occupySlot(availableSlot.getSlotNumber(), vehicle);

        // Generate and persist ticket
        String ticketId = "TKT-" + (++ticketCounter);
        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, 
                availableSlot.getSlotNumber(), availableSlot.getSlotCode(), LocalDateTime.now());

        ticketDao.save(ticket);
        return ticket;
    }

    /**
     * Processes departure, computes duration & tariff, and vacates parking slot.
     */
    public synchronized ParkingTicket processExit(String searchKey) {
        if (searchKey == null || searchKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a Ticket ID or Vehicle Registration Number.");
        }

        ParkingTicket ticket = ticketDao.findActiveByQuery(searchKey.trim());
        if (ticket == null) {
            ParkingTicket completed = ticketDao.findCompletedByQuery(searchKey.trim());
            if (completed != null) {
                throw new IllegalStateException("Ticket '" + completed.getTicketId() + 
                        "' has already exited and been settled at " + completed.getFormattedExitTime());
            }
            throw new NoSuchElementException("No active parked vehicle found matching '" + searchKey.trim() + "'.");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long hours = feeCalculator.calculateBillableHours(ticket.getEntryTime(), exitTime);
        double fee = feeCalculator.calculateFee(ticket.getVehicle().getVehicleType(), ticket.getEntryTime(), exitTime);

        // Complete ticket
        ticket.completeTicket(exitTime, hours, fee);

        // Vacate slot
        slotService.releaseSlot(ticket.getSlotNumber());

        // Update in DAO
        ticketDao.update(ticket);
        return ticket;
    }

    public boolean isVehicleParked(String regNumber) {
        return ticketDao.findActiveByRegNumber(regNumber) != null;
    }

    public ParkingTicket findActiveTicket(String query) {
        return ticketDao.findActiveByQuery(query);
    }

    public ParkingTicket findCompletedTicket(String query) {
        return ticketDao.findCompletedByQuery(query);
    }

    public List<ParkingTicket> getAllTickets() {
        return ticketDao.findAll();
    }

    public List<ParkingTicket> getActiveTickets() {
        return ticketDao.findActiveTickets();
    }

    public List<ParkingTicket> getCompletedTickets() {
        return ticketDao.findCompletedTickets();
    }

    public double getTotalRevenue() {
        double sum = 0.0;
        for (ParkingTicket t : ticketDao.findCompletedTickets()) {
            sum += t.getTotalFee();
        }
        return sum;
    }

    public double getTodayRevenue() {
        LocalDate today = LocalDate.now();
        double sum = 0.0;
        for (ParkingTicket t : ticketDao.findCompletedTickets()) {
            if (t.getExitTime() != null && t.getExitTime().toLocalDate().isEqual(today)) {
                sum += t.getTotalFee();
            }
        }
        return sum;
    }

    public int getTodayCompletedCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (ParkingTicket t : ticketDao.findCompletedTickets()) {
            if (t.getExitTime() != null && t.getExitTime().toLocalDate().isEqual(today)) {
                count++;
            }
        }
        return count;
    }

    public ParkingFeeCalculator getFeeCalculator() {
        return feeCalculator;
    }
}
