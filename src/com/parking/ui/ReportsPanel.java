package com.parking.ui;

import com.parking.model.ParkingTicket;
import com.parking.model.VehicleType;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Reports and revenue analysis panel.
 * Presents daily summaries, vehicle category breakdown, and operational metrics.
 */
public class ReportsPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private JLabel lblTotalServed;
    private JLabel lblCompletedSessions;
    private JLabel lblActiveParked;
    private JLabel lblTotalRevenue;
    private JLabel lblTodayRevenue;

    private DefaultTableModel categoryTableModel;
    private DefaultTableModel dailyRevenueTableModel;

    public ReportsPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        refreshReports();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Parking Reports & Revenue Analytics");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Financial performance, daily transaction summaries, and vehicle type metrics");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Center Content Scroll
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // Top Stat Metric Cards (5 cards)
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblTotalServed = new JLabel("0");
        lblCompletedSessions = new JLabel("0");
        lblActiveParked = new JLabel("0");
        lblTodayRevenue = new JLabel("Rs. 0.00");
        lblTotalRevenue = new JLabel("Rs. 0.00");

        statsGrid.add(createReportStatCard("Total Vehicles", lblTotalServed, UITheme.PRIMARY_BLUE));
        statsGrid.add(createReportStatCard("Completed Exits", lblCompletedSessions, new Color(99, 102, 241)));
        statsGrid.add(createReportStatCard("Currently Parked", lblActiveParked, UITheme.WARNING_AMBER));
        statsGrid.add(createReportStatCard("Today's Revenue", lblTodayRevenue, UITheme.SUCCESS_GREEN));
        statsGrid.add(createReportStatCard("All-Time Revenue", lblTotalRevenue, new Color(16, 185, 129)));

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(20));

        // Middle Row: Vehicle Type Breakdown & Daily Summary
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 20, 0));
        middleRow.setOpaque(false);
        middleRow.setPreferredSize(new Dimension(800, 320));

        // Left Table: Breakdown by Vehicle Type
        JPanel categoryCard = UITheme.createCardPanel();
        categoryCard.setLayout(new BorderLayout(0, 10));

        JLabel catTitle = new JLabel("Revenue Breakdown by Vehicle Category");
        catTitle.setFont(UITheme.FONT_SECTION_TITLE);
        catTitle.setForeground(UITheme.TEXT_DARK);
        categoryCard.add(catTitle, BorderLayout.NORTH);

        String[] catCols = {"Vehicle Category", "Hourly Tariff", "Total Parked", "Active Now", "Revenue Generated"};
        categoryTableModel = new DefaultTableModel(catCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable catTable = new JTable(categoryTableModel);
        UITheme.styleTable(catTable);
        JScrollPane spCat = new JScrollPane(catTable);
        spCat.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        categoryCard.add(spCat, BorderLayout.CENTER);

        // Right Table: Daily Revenue Summary
        JPanel dailyCard = UITheme.createCardPanel();
        dailyCard.setLayout(new BorderLayout(0, 10));

        JLabel dailyTitle = new JLabel("Daily Revenue Summary");
        dailyTitle.setFont(UITheme.FONT_SECTION_TITLE);
        dailyTitle.setForeground(UITheme.TEXT_DARK);
        dailyCard.add(dailyTitle, BorderLayout.NORTH);

        String[] dailyCols = {"Date", "Exited Sessions", "Total Collection (Rs.)"};
        dailyRevenueTableModel = new DefaultTableModel(dailyCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable dailyTable = new JTable(dailyRevenueTableModel);
        UITheme.styleTable(dailyTable);
        JScrollPane spDaily = new JScrollPane(dailyTable);
        spDaily.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        dailyCard.add(spDaily, BorderLayout.CENTER);

        middleRow.add(categoryCard);
        middleRow.add(dailyCard);

        centerContent.add(middleRow);

        JScrollPane mainScroll = new JScrollPane(centerContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);

        add(headerPanel, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createReportStatCard(String title, JLabel valLabel, Color accent) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_BADGE);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valLabel.setFont(UITheme.FONT_CARD_VALUE);
        valLabel.setForeground(UITheme.TEXT_DARK);

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(10, 4));
        strip.setBackground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(strip, BorderLayout.SOUTH);

        return card;
    }

    public void refreshReports() {
        List<ParkingTicket> allTickets = manager.getAllTickets();
        List<ParkingTicket> completed = manager.getCompletedTickets();
        List<ParkingTicket> active = manager.getActiveTickets();

        lblTotalServed.setText(String.valueOf(allTickets.size()));
        lblCompletedSessions.setText(String.valueOf(completed.size()));
        lblActiveParked.setText(String.valueOf(active.size()));
        lblTodayRevenue.setText(String.format("Rs. %.2f", manager.getTodayRevenue()));
        lblTotalRevenue.setText(String.format("Rs. %.2f", manager.getTotalRevenue()));

        // Populate Category Breakdown
        categoryTableModel.setRowCount(0);
        for (VehicleType vt : VehicleType.values()) {
            int totalCount = 0;
            int activeCount = 0;
            double revenue = 0.0;

            for (ParkingTicket t : allTickets) {
                if (t.getVehicle().getVehicleType() == vt) {
                    totalCount++;
                    if (t.isActive()) {
                        activeCount++;
                    } else {
                        revenue += t.getTotalFee();
                    }
                }
            }

            categoryTableModel.addRow(new Object[]{
                    vt.getDisplayName(),
                    String.format("Rs. %.2f / hr", manager.getTicketService().getFeeCalculator().getHourlyRate(vt)),
                    totalCount,
                    activeCount,
                    String.format("Rs. %.2f", revenue)
            });
        }

        // Populate Daily Revenue Summary
        dailyRevenueTableModel.setRowCount(0);
        Map<LocalDate, Double> dailyRevenueMap = new TreeMap<>(Collections.reverseOrder());
        Map<LocalDate, Integer> dailyCountMap = new HashMap<>();

        for (ParkingTicket t : completed) {
            if (t.getExitTime() != null) {
                LocalDate date = t.getExitTime().toLocalDate();
                dailyRevenueMap.put(date, dailyRevenueMap.getOrDefault(date, 0.0) + t.getTotalFee());
                dailyCountMap.put(date, dailyCountMap.getOrDefault(date, 0) + 1);
            }
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy (EEE)");
        if (dailyRevenueMap.isEmpty()) {
            // Include today with 0 if no completed tickets
            dailyRevenueTableModel.addRow(new Object[]{
                    LocalDate.now().format(dateFmt) + " [Today]",
                    0,
                    "Rs. 0.00"
            });
        } else {
            for (Map.Entry<LocalDate, Double> entry : dailyRevenueMap.entrySet()) {
                String label = entry.getKey().format(dateFmt);
                if (entry.getKey().isEqual(LocalDate.now())) {
                    label += " [Today]";
                }
                dailyRevenueTableModel.addRow(new Object[]{
                        label,
                        dailyCountMap.getOrDefault(entry.getKey(), 0),
                        String.format("Rs. %.2f", entry.getValue())
                });
            }
        }
    }
}
