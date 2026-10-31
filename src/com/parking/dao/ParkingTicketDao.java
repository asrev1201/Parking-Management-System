package com.parking.dao;

import com.parking.model.ParkingTicket;

import java.util.List;

/**
 * Data Access Object (DAO) interface for ParkingTicket persistence operations.
 * Defines standard CRUD operations without exposing database implementation details.
 */
public interface ParkingTicketDao {

    /**
     * Inserts a newly generated parking ticket into persistent storage.
     */
    void save(ParkingTicket ticket);

    /**
     * Updates an existing ticket upon vehicle exit (settled timestamps, fee, status).
     */
    void update(ParkingTicket ticket);

    /**
     * Finds a ticket by its primary key identifier.
     */
    ParkingTicket findById(String ticketId);

    /**
     * Finds an active parking ticket matching a vehicle's registration plate.
     */
    ParkingTicket findActiveByRegNumber(String regNumber);

    /**
     * Searches for an active ticket by Ticket ID or Vehicle Registration Number.
     */
    ParkingTicket findActiveByQuery(String query);

    /**
     * Searches for a completed ticket by Ticket ID or Vehicle Registration Number.
     */
    ParkingTicket findCompletedByQuery(String query);

    /**
     * Retrieves all parking ticket records (both active and completed).
     */
    List<ParkingTicket> findAll();

    /**
     * Retrieves currently active (parked) tickets.
     */
    List<ParkingTicket> findActiveTickets();

    /**
     * Retrieves completed (exited) tickets.
     */
    List<ParkingTicket> findCompletedTickets();

    /**
     * Deletes a ticket by ID (administrative cleanup).
     */
    void delete(String ticketId);
}
