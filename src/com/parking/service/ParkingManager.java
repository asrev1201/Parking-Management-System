package com.parking.service;

import com.parking.dao.*;
import com.parking.data.DatabaseInitializer;
import com.parking.model.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinating Facade for the Parking System services.
 * Hides database initialization and delegates all business operations
 * directly to ParkingSlotService and ParkingTicketService.
 */
public class ParkingManager {
    private final ParkingSlotDao slotDao;
    private final ParkingTicketDao ticketDao;
    private final ParkingSlotService slotService;
    private final ParkingTicketService ticketService;
    private final ParkingFeeCalculator feeCalculator;
    private final List<Runnable> listeners;

    public ParkingManager() {
        // 1. Completely isolated database initialization
        DatabaseInitializer.initialize();

        // 2. Instantiate DAOs
        this.slotDao = new ParkingSlotDaoImpl();
        this.ticketDao = new ParkingTicketDaoImpl();

        // 3. Instantiate Business Services
        this.feeCalculator = new ParkingFeeCalculator();
        this.slotService = new ParkingSlotService(slotDao);
        this.ticketService = new ParkingTicketService(ticketDao, slotService, feeCalculator);
        this.listeners = new CopyOnWriteArrayList<>();

        // 4. Restore active parked vehicle states in memory slots
        syncActiveVehiclesToSlots();
    }

    private void syncActiveVehiclesToSlots() {
        for (ParkingTicket t : ticketService.getActiveTickets()) {
            ParkingSlot slot = slotService.getSlotByNumber(t.getSlotNumber());
            if (slot != null && slot.isAvailable()) {
                slot.assignVehicle(t.getVehicle());
            }
        }
    }

    /**
     * Registers a new vehicle into the system via ParkingTicketService.
     */
    public synchronized ParkingTicket parkVehicle(String regNumber, String ownerName, VehicleType vehicleType) {
        ParkingTicket ticket = ticketService.issueTicket(regNumber, ownerName, vehicleType);
        notifyListeners();
        return ticket;
    }

    /**
     * Processes vehicle departure and fee calculation via ParkingTicketService.
     */
    public synchronized ParkingTicket exitVehicle(String searchKey) {
        ParkingTicket ticket = ticketService.processExit(searchKey);
        notifyListeners();
        return ticket;
    }

    // Clean service getters for UI controllers
    public ParkingSlotService getSlotService() {
        return slotService;
    }

    public ParkingTicketService getTicketService() {
        return ticketService;
    }

    public ParkingFeeCalculator getFeeCalculator() {
        return feeCalculator;
    }

    // Facade query delegations
    public ParkingTicket findActiveTicket(String query) {
        return ticketService.findActiveTicket(query);
    }

    public ParkingTicket findCompletedTicket(String query) {
        return ticketService.findCompletedTicket(query);
    }

    public List<ParkingTicket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    public List<ParkingTicket> getActiveTickets() {
        return ticketService.getActiveTickets();
    }

    public List<ParkingTicket> getCompletedTickets() {
        return ticketService.getCompletedTickets();
    }

    public double getTotalRevenue() {
        return ticketService.getTotalRevenue();
    }

    public double getTodayRevenue() {
        return ticketService.getTodayRevenue();
    }

    public void addListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void notifyListeners() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
