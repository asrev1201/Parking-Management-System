package com.parking.ui;

import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window and navigation controller.
 * Hosts the sidebar menu, header status with live clock, and card views.
 * Completely free of database console UI or raw SQL exposure.
 */
public class MainFrame extends JFrame {
    public static final String VIEW_DASHBOARD = "DASHBOARD";
    public static final String VIEW_ENTRY = "ENTRY";
    public static final String VIEW_EXIT = "EXIT";
    public static final String VIEW_SLOTS = "SLOTS";
    public static final String VIEW_RECORDS = "RECORDS";
    public static final String VIEW_REPORTS = "REPORTS";

    private final ParkingManager manager;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private DashboardPanel dashboardPanel;
    private VehicleEntryPanel vehicleEntryPanel;
    private VehicleExitPanel vehicleExitPanel;
    private ParkingSlotsPanel parkingSlotsPanel;
    private RecordsPanel recordsPanel;
    private ReportsPanel reportsPanel;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentView = VIEW_DASHBOARD;
    private JLabel lblLiveClock;
    private JLabel lblSidebarStatus;

    public MainFrame(ParkingManager manager) {
        super("ParkSmart - Parking Management System");
        this.manager = manager;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        initComponents();

        // Register manager listener for synchronized real-time UI updates
        manager.addListener(this::onDataChanged);

        // Start live clock timer (updates every second)
        startClockTimer();

        // Initial view
        showView(VIEW_DASHBOARD);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Sidebar Navigation (West)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Right Container (Top Bar + Center Content)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UITheme.CONTENT_BG);

        // Top Status Header Bar
        JPanel topHeader = createTopHeader();
        rightContainer.add(topHeader, BorderLayout.NORTH);

        // Center Views (CardLayout)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.CONTENT_BG);

        dashboardPanel = new DashboardPanel(manager, this);
        vehicleEntryPanel = new VehicleEntryPanel(manager, this);
        vehicleExitPanel = new VehicleExitPanel(manager, this);
        parkingSlotsPanel = new ParkingSlotsPanel(manager, this);
        recordsPanel = new RecordsPanel(manager, this);
        reportsPanel = new ReportsPanel(manager, this);

        cardsPanel.add(dashboardPanel, VIEW_DASHBOARD);
        cardsPanel.add(vehicleEntryPanel, VIEW_ENTRY);
        cardsPanel.add(vehicleExitPanel, VIEW_EXIT);
        cardsPanel.add(parkingSlotsPanel, VIEW_SLOTS);
        cardsPanel.add(recordsPanel, VIEW_RECORDS);
        cardsPanel.add(reportsPanel, VIEW_REPORTS);

        rightContainer.add(cardsPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 800));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new BorderLayout(8, 5));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(5, 5, 25, 5));

        JLabel brandIcon = new JLabel("P", SwingConstants.CENTER);
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandIcon.setForeground(Color.WHITE);
        brandIcon.setBackground(UITheme.PRIMARY_BLUE);
        brandIcon.setOpaque(true);
        brandIcon.setPreferredSize(new Dimension(38, 38));
        brandIcon.setBorder(BorderFactory.createLineBorder(new Color(96, 165, 250), 1, true));

        JLabel brandTitle = new JLabel("PARKSMART");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandTitle.setForeground(UITheme.SIDEBAR_TEXT);

        JLabel brandSub = new JLabel("Smart Parking Terminal");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(UITheme.SIDEBAR_MUTED);

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(brandTitle);
        textWrapper.add(brandSub);

        brandPanel.add(brandIcon, BorderLayout.WEST);
        brandPanel.add(textWrapper, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Menu Navigation Buttons
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 0, 10));
        menuPanel.setOpaque(false);

        menuPanel.add(createNavButton("Dashboard", VIEW_DASHBOARD));
        menuPanel.add(createNavButton("Vehicle Entry", VIEW_ENTRY));
        menuPanel.add(createNavButton("Vehicle Exit", VIEW_EXIT));
        menuPanel.add(createNavButton("Parking Slots", VIEW_SLOTS));
        menuPanel.add(createNavButton("Vehicle Records", VIEW_RECORDS));
        menuPanel.add(createNavButton("Reports & Revenue", VIEW_REPORTS));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Bottom Operational Status Widget (Clean, Professional, No Database Details Exposed)
        JPanel bottomWidget = new JPanel(new BorderLayout(5, 5));
        bottomWidget.setBackground(UITheme.SIDEBAR_HOVER);
        bottomWidget.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblSystemStatus = new JLabel("SYSTEM STATUS: ONLINE");
        lblSystemStatus.setFont(UITheme.FONT_BADGE);
        lblSystemStatus.setForeground(UITheme.SUCCESS_GREEN);

        lblSidebarStatus = new JLabel("Available: " + manager.getSlotService().getAvailableCount() + " bays");
        lblSidebarStatus.setFont(UITheme.FONT_REGULAR);
        lblSidebarStatus.setForeground(UITheme.SIDEBAR_TEXT);

        bottomWidget.add(lblSystemStatus, BorderLayout.NORTH);
        bottomWidget.add(lblSidebarStatus, BorderLayout.CENTER);

        sidebar.add(bottomWidget, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String title, String viewName) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(UITheme.SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(UITheme.SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> showView(viewName));

        navButtons.put(viewName, btn);
        return btn;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(12, 25, 12, 25)
        ));

        JLabel title = new JLabel("Parking Management System | Secure Architecture");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(UITheme.TEXT_DARK);

        lblLiveClock = new JLabel("Clock loading...");
        lblLiveClock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLiveClock.setForeground(UITheme.PRIMARY_BLUE);

        header.add(title, BorderLayout.WEST);
        header.add(lblLiveClock, BorderLayout.EAST);

        return header;
    }

    private void startClockTimer() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  |  HH:mm:ss");
        Timer timer = new Timer(1000, e -> {
            lblLiveClock.setText(LocalDateTime.now().format(fmt));
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    public void showView(String viewName) {
        this.currentView = viewName;
        cardLayout.show(cardsPanel, viewName);

        // Update nav buttons active highlight
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewName)) {
                entry.getValue().setBackground(UITheme.SIDEBAR_ACTIVE);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(UITheme.SIDEBAR_BG);
                entry.getValue().setForeground(UITheme.SIDEBAR_TEXT);
            }
        }

        switch (viewName) {
            case VIEW_DASHBOARD:
                dashboardPanel.refreshData();
                break;
            case VIEW_ENTRY:
                vehicleEntryPanel.updateAvailability();
                break;
            case VIEW_EXIT:
                vehicleExitPanel.refreshActiveVehicles();
                break;
            case VIEW_SLOTS:
                parkingSlotsPanel.refreshSlots();
                break;
            case VIEW_RECORDS:
                recordsPanel.refreshRecords();
                break;
            case VIEW_REPORTS:
                reportsPanel.refreshReports();
                break;
        }

        updateSidebarStatus();
    }

    private void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refreshData();
            vehicleEntryPanel.updateAvailability();
            vehicleExitPanel.refreshActiveVehicles();
            parkingSlotsPanel.refreshSlots();
            recordsPanel.refreshRecords();
            reportsPanel.refreshReports();
            updateSidebarStatus();
        });
    }

    private void updateSidebarStatus() {
        if (lblSidebarStatus != null) {
            int avail = manager.getSlotService().getAvailableCount();
            int total = manager.getSlotService().getTotalSlotsCount();
            lblSidebarStatus.setText("Available: " + avail + " / " + total + " days");
        }
    }
}
