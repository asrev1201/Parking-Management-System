package com.parking.ui;

import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.Vehicle;
import com.parking.model.VehicleType;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visual interactive grid displaying all physical parking slots.
 * Features color-coded vacant (green) and occupied (red) slot bays,
 * multi-category filtering, real-time occupancy statistics, and click-to-view slot details.
 */
public class ParkingSlotsPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private JPanel gridContainer;
    private String currentFilter = "ALL";
    private JLabel lblOverviewStats;
    private final Map<String, JButton> filterButtons = new HashMap<>();

    public ParkingSlotsPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        refreshSlots();
    }

    private void initUI() {
        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Parking Slots Visual Grid");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Interactive visual map of designated parking bays with real-time occupancy status");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // 2. Filter Bar & Legend Controls Card
        JPanel controlsCard = UITheme.createCardPanel();
        controlsCard.setLayout(new BorderLayout(0, 10));

        // Row 1: Filter Toggle Buttons
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        addFilterButton(filterRow, "All Slots (30)", "ALL");
        addFilterButton(filterRow, "Bikes (10)", "BIKE");
        addFilterButton(filterRow, "Cars (15)", "CAR");
        addFilterButton(filterRow, "Buses (5)", "BUS");
        addFilterButton(filterRow, "Available Only", "AVAILABLE");
        addFilterButton(filterRow, "Occupied Only", "OCCUPIED");

        // Row 2: Occupancy Stats & Legend
        JPanel statsRow = new JPanel(new BorderLayout());
        statsRow.setOpaque(false);

        lblOverviewStats = new JLabel("Occupancy: 0 / 30 (0%)");
        lblOverviewStats.setFont(UITheme.FONT_BOLD);
        lblOverviewStats.setForeground(UITheme.TEXT_DARK);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legendPanel.setOpaque(false);

        JLabel legVacant = new JLabel("[Available / Vacant]");
        legVacant.setFont(UITheme.FONT_BOLD);
        legVacant.setForeground(new Color(5, 150, 105));

        JLabel legOcc = new JLabel("[Occupied / Parked]");
        legOcc.setFont(UITheme.FONT_BOLD);
        legOcc.setForeground(new Color(220, 38, 38));

        legendPanel.add(legVacant);
        legendPanel.add(legOcc);

        statsRow.add(lblOverviewStats, BorderLayout.WEST);
        statsRow.add(legendPanel, BorderLayout.EAST);

        controlsCard.add(filterRow, BorderLayout.NORTH);
        controlsCard.add(statsRow, BorderLayout.SOUTH);

        // 3. Grid Container inside ScrollPane
        gridContainer = new JPanel();
        gridContainer.setLayout(new GridLayout(0, 5, 14, 14));
        gridContainer.setBackground(UITheme.CONTENT_BG);
        gridContainer.setBorder(new EmptyBorder(10, 5, 15, 5));

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.CONTENT_BG);
        scrollPane.getViewport().setBackground(UITheme.CONTENT_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(controlsCard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void addFilterButton(JPanel parent, String label, String filterKey) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 32));

        btn.addActionListener(e -> {
            this.currentFilter = filterKey;
            updateFilterButtonStyles();
            refreshSlots();
        });

        filterButtons.put(filterKey, btn);
        parent.add(btn);
        updateFilterButtonStyles();
    }

    private void updateFilterButtonStyles() {
        for (Map.Entry<String, JButton> entry : filterButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(currentFilter)) {
                btn.setBackground(UITheme.PRIMARY_BLUE);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                btn.setOpaque(true);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(UITheme.TEXT_DARK);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                        BorderFactory.createEmptyBorder(5, 11, 5, 11)
                ));
                btn.setOpaque(true);
            }
        }
    }

    public void refreshSlots() {
        gridContainer.removeAll();
        List<ParkingSlot> allSlots = manager.getSlotService().getAllSlots();

        int displayedCount = 0;
        int occupiedCount = manager.getSlotService().getOccupiedCount();
        int totalCount = manager.getSlotService().getTotalSlotsCount();
        int pct = totalCount > 0 ? (occupiedCount * 100 / totalCount) : 0;
        lblOverviewStats.setText(String.format("Occupancy: %d of %d slots occupied (%d%%)", occupiedCount, totalCount, pct));

        for (ParkingSlot slot : allSlots) {
            // Apply filtering logic
            if (currentFilter.equals("BIKE") && slot.getDesignatedType() != VehicleType.BIKE) continue;
            if (currentFilter.equals("CAR") && slot.getDesignatedType() != VehicleType.CAR) continue;
            if (currentFilter.equals("BUS") && slot.getDesignatedType() != VehicleType.BUS) continue;
            if (currentFilter.equals("AVAILABLE") && slot.isOccupied()) continue;
            if (currentFilter.equals("OCCUPIED") && !slot.isOccupied()) continue;

            gridContainer.add(createSlotCard(slot));
            displayedCount++;
        }

        if (displayedCount == 0) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel lblEmpty = new JLabel("No parking slots match the selected filter (" + currentFilter + ")");
            lblEmpty.setFont(UITheme.FONT_SECTION_TITLE);
            lblEmpty.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(lblEmpty);

            gridContainer.setLayout(new BorderLayout());
            gridContainer.add(emptyPanel, BorderLayout.CENTER);
        } else {
            // 5 columns layout with ample breathing room
            gridContainer.setLayout(new GridLayout(0, 5, 14, 14));
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createSlotCard(ParkingSlot slot) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(175, 135));
        card.setMinimumSize(new Dimension(160, 130));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean isOccupied = slot.isOccupied();
        Color borderColor = isOccupied ? new Color(248, 113, 113) : new Color(110, 231, 183);
        Color bgColor = isOccupied ? new Color(254, 242, 242) : new Color(240, 253, 244);

        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // 1. Top Bar: Slot Code + Vehicle Category Pill
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblCode = new JLabel(slot.getSlotCode());
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCode.setForeground(UITheme.TEXT_DARK);

        JLabel lblType = new JLabel(" " + slot.getDesignatedType().getDisplayName().toUpperCase() + " ");
        lblType.setFont(UITheme.FONT_BADGE);
        lblType.setForeground(new Color(71, 85, 105));
        lblType.setOpaque(true);
        lblType.setBackground(new Color(226, 232, 240));
        lblType.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        top.add(lblCode, BorderLayout.WEST);
        top.add(lblType, BorderLayout.EAST);

        // 2. Center: Vehicle details or Vacant status
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 3));
        center.setOpaque(false);

        if (isOccupied && slot.getCurrentVehicle() != null) {
            JLabel lblReg = new JLabel(slot.getCurrentVehicle().getRegistrationNumber());
            lblReg.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblReg.setForeground(new Color(185, 28, 28));

            JLabel lblOwner = new JLabel("Owner: " + slot.getCurrentVehicle().getOwnerName());
            lblOwner.setFont(UITheme.FONT_BADGE);
            lblOwner.setForeground(UITheme.TEXT_MUTED);

            center.add(lblReg);
            center.add(lblOwner);
        } else {
            JLabel lblStatus = new JLabel("VACANT");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStatus.setForeground(new Color(5, 150, 105));

            double rate = manager.getTicketService().getFeeCalculator().getHourlyRate(slot.getDesignatedType());
            JLabel lblTariff = new JLabel("Tariff: Rs. " + String.format("%.0f", rate) + "/hr");
            lblTariff.setFont(UITheme.FONT_BADGE);
            lblTariff.setForeground(UITheme.TEXT_MUTED);

            center.add(lblStatus);
            center.add(lblTariff);
        }

        // 3. Bottom Strip: Status Indicator
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel lblBottom = new JLabel(isOccupied ? "OCCUPIED" : "AVAILABLE FOR USE");
        lblBottom.setFont(UITheme.FONT_BADGE);
        lblBottom.setForeground(isOccupied ? new Color(220, 38, 38) : new Color(5, 150, 105));
        bottom.add(lblBottom, BorderLayout.WEST);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        // Hover Effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isOccupied ? UITheme.DANGER_RED : UITheme.SUCCESS_GREEN, 2, true),
                        new EmptyBorder(12, 12, 12, 12)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 2, true),
                        new EmptyBorder(12, 12, 12, 12)
                ));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showSlotDetailsDialog(slot);
            }
        });

        return card;
    }

    private void showSlotDetailsDialog(ParkingSlot slot) {
        if (!slot.isOccupied()) {
            double rate = manager.getTicketService().getFeeCalculator().getHourlyRate(slot.getDesignatedType());
            String msg = "Slot Code: " + slot.getSlotCode() + " (Slot #" + slot.getSlotNumber() + ")\n" +
                    "Designated Vehicle Category: " + slot.getDesignatedType().getDisplayName() + "\n" +
                    "Standard Tariff: Rs. " + String.format("%.2f", rate) + " / hour\n\n" +
                    "Status: VACANT & READY FOR PARKING";

            JOptionPane.showMessageDialog(this,
                    msg,
                    "Slot " + slot.getSlotCode() + " Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Occupied slot details
        Vehicle v = slot.getCurrentVehicle();
        ParkingTicket ticket = null;
        for (ParkingTicket t : manager.getActiveTickets()) {
            if (t.getSlotNumber() == slot.getSlotNumber()) {
                ticket = t;
                break;
            }
        }

        String msg = "Slot Code: " + slot.getSlotCode() + " (Slot #" + slot.getSlotNumber() + ")\n" +
                "Vehicle Type: " + slot.getDesignatedType().getDisplayName() + "\n" +
                "Registration No: " + (v != null ? v.getRegistrationNumber() : "Unknown") + "\n" +
                "Owner Name: " + (v != null ? v.getOwnerName() : "Unknown") + "\n" +
                "Entry Time: " + (ticket != null ? ticket.getFormattedEntryTime() : "Unknown") + "\n" +
                "Ticket ID: " + (ticket != null ? ticket.getTicketId() : "N/A") + "\n\n" +
                "Would you like to navigate to Vehicle Exit for this vehicle?";

        int opt = JOptionPane.showConfirmDialog(this, msg,
                "Occupied Slot Details - " + slot.getSlotCode(),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION && ticket != null) {
            mainFrame.showView(MainFrame.VIEW_EXIT);
        }
    }
}
