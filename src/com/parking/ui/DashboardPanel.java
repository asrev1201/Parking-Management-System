package com.parking.ui;

import com.parking.model.ParkingTicket;
import com.parking.model.VehicleType;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel. Calls clean service methods; zero raw SQL or storage access.
 */
public class DashboardPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private JLabel lblTotalSlots;
    private JLabel lblOccupiedSlots;
    private JLabel lblAvailableSlots;
    private JLabel lblCurrentlyParked;
    private JLabel lblTotalRevenue;

    private JProgressBar bikeProgress;
    private JProgressBar carProgress;
    private JProgressBar busProgress;
    private JLabel lblBikeRatio;
    private JLabel lblCarRatio;
    private JLabel lblBusRatio;

    private DefaultTableModel recentTableModel;

    public DashboardPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        refreshData();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("System Dashboard");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Real-time overview of parking capacity, revenue, and recent vehicle activity");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // Metric Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblTotalSlots     = new JLabel("0");
        lblOccupiedSlots  = new JLabel("0");
        lblAvailableSlots = new JLabel("0");
        lblCurrentlyParked = new JLabel("0");
        lblTotalRevenue   = new JLabel("Rs. 0.00");

        statsGrid.add(createStatCard("Total Slots",     lblTotalSlots,      new Color(59, 130, 246), "Capacity"));
        statsGrid.add(createStatCard("Occupied",        lblOccupiedSlots,   UITheme.DANGER_RED,      "Parked"));
        statsGrid.add(createStatCard("Available",       lblAvailableSlots,  UITheme.SUCCESS_GREEN,   "Ready"));
        statsGrid.add(createStatCard("Active Vehicles", lblCurrentlyParked, new Color(139, 92, 246), "On-Site"));
        statsGrid.add(createStatCard("Total Revenue",   lblTotalRevenue,    new Color(245, 158, 11), "Collected"));

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(20));

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 20, 0));
        middleRow.setOpaque(false);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        middleRow.add(createCapacityCard());
        middleRow.add(createQuickActionsCard());

        centerContent.add(middleRow);
        centerContent.add(Box.createVerticalStrut(20));
        centerContent.add(createRecentActivityCard());

        JScrollPane scrollPane = new JScrollPane(centerContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String badgeText) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_BADGE);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        JLabel badge = new JLabel(" " + badgeText + " ");
        badge.setFont(UITheme.FONT_BADGE);
        badge.setOpaque(true);
        badge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
        badge.setForeground(accentColor);
        badge.setBorder(BorderFactory.createLineBorder(
                new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80), 1, true));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        valueLabel.setFont(UITheme.FONT_CARD_VALUE);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(10, 4));
        strip.setBackground(accentColor);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(strip, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCapacityCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Slot Utilization by Vehicle Type");
        title.setFont(UITheme.FONT_SECTION_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel barsPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        barsPanel.setOpaque(false);

        JPanel bikeRow = new JPanel(new BorderLayout(10, 2));
        bikeRow.setOpaque(false);
        JLabel lblBikeTitle = new JLabel("Bikes (Two-Wheelers)");
        lblBikeTitle.setFont(UITheme.FONT_REGULAR);
        lblBikeRatio = new JLabel("0 / 10");
        lblBikeRatio.setFont(UITheme.FONT_BOLD);
        bikeProgress = new JProgressBar(0, 10);
        bikeProgress.setForeground(new Color(59, 130, 246));
        bikeRow.add(lblBikeTitle, BorderLayout.WEST);
        bikeRow.add(lblBikeRatio, BorderLayout.EAST);
        bikeRow.add(bikeProgress, BorderLayout.SOUTH);

        JPanel carRow = new JPanel(new BorderLayout(10, 2));
        carRow.setOpaque(false);
        JLabel lblCarTitle = new JLabel("Cars (Four-Wheelers)");
        lblCarTitle.setFont(UITheme.FONT_REGULAR);
        lblCarRatio = new JLabel("0 / 15");
        lblCarRatio.setFont(UITheme.FONT_BOLD);
        carProgress = new JProgressBar(0, 15);
        carProgress.setForeground(UITheme.PRIMARY_BLUE);
        carRow.add(lblCarTitle, BorderLayout.WEST);
        carRow.add(lblCarRatio, BorderLayout.EAST);
        carRow.add(carProgress, BorderLayout.SOUTH);

        JPanel busRow = new JPanel(new BorderLayout(10, 2));
        busRow.setOpaque(false);
        JLabel lblBusTitle = new JLabel("Buses (Heavy Vehicles)");
        lblBusTitle.setFont(UITheme.FONT_REGULAR);
        lblBusRatio = new JLabel("0 / 5");
        lblBusRatio.setFont(UITheme.FONT_BOLD);
        busProgress = new JProgressBar(0, 5);
        busProgress.setForeground(new Color(245, 158, 11));
        busRow.add(lblBusTitle, BorderLayout.WEST);
        busRow.add(lblBusRatio, BorderLayout.EAST);
        busRow.add(busProgress, BorderLayout.SOUTH);

        barsPanel.add(bikeRow);
        barsPanel.add(carRow);
        barsPanel.add(busRow);

        card.add(barsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Quick Actions & Navigation");
        title.setFont(UITheme.FONT_SECTION_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonsGrid.setOpaque(false);

        JButton btnEntry = UITheme.createButton("Vehicle Entry", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnEntry.addActionListener(e -> mainFrame.showView(MainFrame.VIEW_ENTRY));

        JButton btnExit = UITheme.createButton("Vehicle Exit / Bill", UITheme.DANGER_RED, Color.WHITE);
        btnExit.addActionListener(e -> mainFrame.showView(MainFrame.VIEW_EXIT));

        JButton btnSlots = UITheme.createButton("View Parking Grid", new Color(79, 70, 229), Color.WHITE);
        btnSlots.addActionListener(e -> mainFrame.showView(MainFrame.VIEW_SLOTS));

        JButton btnReports = UITheme.createButton("Revenue Reports", new Color(16, 185, 129), Color.WHITE);
        btnReports.addActionListener(e -> mainFrame.showView(MainFrame.VIEW_REPORTS));

        buttonsGrid.add(btnEntry);
        buttonsGrid.add(btnExit);
        buttonsGrid.add(btnSlots);
        buttonsGrid.add(btnReports);

        card.add(buttonsGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRecentActivityCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Recent Vehicles in Facility");
        title.setFont(UITheme.FONT_SECTION_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JButton btnViewAll = new JButton("View All Records");
        btnViewAll.setFont(UITheme.FONT_BOLD);
        btnViewAll.setForeground(UITheme.PRIMARY_BLUE);
        btnViewAll.setContentAreaFilled(false);
        btnViewAll.setBorderPainted(false);
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.addActionListener(e -> mainFrame.showView(MainFrame.VIEW_RECORDS));

        top.add(title, BorderLayout.WEST);
        top.add(btnViewAll, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        String[] columns = {"Ticket ID", "Registration No", "Owner Name", "Type", "Slot", "Entry Time", "Status"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(recentTableModel);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(sp.getPreferredSize().width, 180));
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    /** Called by MainFrame whenever data changes. Uses clean service calls only. */
    public void refreshData() {
        // All stats via ParkingSlotService and ParkingTicketService — no SQL in the UI
        int totalSlots    = manager.getSlotService().getTotalSlotsCount();
        int occupiedSlots = manager.getSlotService().getOccupiedCount();
        int availSlots    = manager.getSlotService().getAvailableCount();
        int activeVeh     = manager.getActiveTickets().size();
        double revenue    = manager.getTotalRevenue();

        lblTotalSlots.setText(String.valueOf(totalSlots));
        lblOccupiedSlots.setText(String.valueOf(occupiedSlots));
        lblAvailableSlots.setText(String.valueOf(availSlots));
        lblCurrentlyParked.setText(String.valueOf(activeVeh));
        lblTotalRevenue.setText(String.format("Rs. %.2f", revenue));

        int bikeOcc = manager.getSlotService().getOccupiedCountByType(VehicleType.BIKE);
        int bikeTot = manager.getSlotService().getTotalSlotsCount(VehicleType.BIKE);
        bikeProgress.setMaximum(Math.max(1, bikeTot));
        bikeProgress.setValue(bikeOcc);
        lblBikeRatio.setText(bikeOcc + " / " + bikeTot + " occupied");

        int carOcc = manager.getSlotService().getOccupiedCountByType(VehicleType.CAR);
        int carTot = manager.getSlotService().getTotalSlotsCount(VehicleType.CAR);
        carProgress.setMaximum(Math.max(1, carTot));
        carProgress.setValue(carOcc);
        lblCarRatio.setText(carOcc + " / " + carTot + " occupied");

        int busOcc = manager.getSlotService().getOccupiedCountByType(VehicleType.BUS);
        int busTot = manager.getSlotService().getTotalSlotsCount(VehicleType.BUS);
        busProgress.setMaximum(Math.max(1, busTot));
        busProgress.setValue(busOcc);
        lblBusRatio.setText(busOcc + " / " + busTot + " occupied");

        recentTableModel.setRowCount(0);
        List<ParkingTicket> all = manager.getAllTickets();
        int start = Math.max(0, all.size() - 6);
        for (int i = all.size() - 1; i >= start; i--) {
            ParkingTicket t = all.get(i);
            recentTableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getRegistrationNumber(),
                    t.getVehicle().getOwnerName(),
                    t.getVehicle().getVehicleType().getDisplayName(),
                    t.getSlotCode(),
                    t.getFormattedEntryTime(),
                    t.getStatus()
            });
        }
    }
}
