package com.parking.service;

import com.parking.model.VehicleType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service class responsible for computing parking duration and fees.
 * Applies parking policy: minimum 1 hour, rounding up partial hours.
 */
public class ParkingFeeCalculator {

    // Configurable rates map
    private final Map<VehicleType, Double> hourlyRates;

    public ParkingFeeCalculator() {
        hourlyRates = new EnumMap<>(VehicleType.class);
        hourlyRates.put(VehicleType.BIKE, 20.0);
        hourlyRates.put(VehicleType.CAR, 50.0);
        hourlyRates.put(VehicleType.BUS, 100.0);
    }

    /**
     * Calculates billable hours based on entry and exit time.
     * Enforces the rule: minimum 1 hour, round up any partial hour.
     *
     * @param entryTime Time when vehicle entered
     * @param exitTime Time when vehicle exited
     * @return Number of billable whole hours
     */
    public long calculateBillableHours(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            return 1;
        }
        if (exitTime.isBefore(entryTime)) {
            // Guard against system clock anomaly or negative duration
            return 1;
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();

        // Any duration less than or equal to 60 minutes is billed as 1 hour
        if (minutes <= 60) {
            return 1;
        }

        // Round up partial hours (e.g., 61 minutes -> 2 hours)
        return (long) Math.ceil((double) minutes / 60.0);
    }

    /**
     * Calculates the total parking fee.
     *
     * @param type Vehicle type
     * @param entryTime Time vehicle entered
     * @param exitTime Time vehicle exited
     * @return Total parking fee in INR
     */
    public double calculateFee(VehicleType type, LocalDateTime entryTime, LocalDateTime exitTime) {
        long hours = calculateBillableHours(entryTime, exitTime);
        double rate = getHourlyRate(type);
        return hours * rate;
    }

    public double getHourlyRate(VehicleType type) {
        return hourlyRates.getOrDefault(type, type.getDefaultHourlyRate());
    }

    public void setHourlyRate(VehicleType type, double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative.");
        }
        hourlyRates.put(type, rate);
    }

    /**
     * Generates a readable human description of actual elapsed duration.
     */
    public String getExactDurationString(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null || exitTime.isBefore(entryTime)) {
            return "0 mins";
        }
        Duration d = Duration.between(entryTime, exitTime);
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
