package com.parking.ui;

import com.parking.model.ParkingTicket;
import com.parking.model.VehicleType;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Handles new vehicle registration, input validation, slot allocation,
 * and generation of the parking ticket receipt.
 */
public class VehicleEntryPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private JTextField txtRegNumber;
    private JTextField txtOwnerName;
    private JComboBox<VehicleType> cmbVehicleType;
    private JLabel lblSlotAvailabilityInfo;
    private JLabel lblHourlyRateInfo;

    public VehicleEntryPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        updateAvailability();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Vehicle Entry & Ticket Generation");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Register a new incoming vehicle and automatically allocate an available parking slot");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Center Form Container
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel card = UITheme.createCardPanel();
        card.setPreferredSize(new Dimension(580, 520));
        card.setLayout(new BorderLayout(0, 20));

        // Card Title
        JLabel formTitle = new JLabel("Vehicle Registration Form");
        formTitle.setFont(UITheme.FONT_SECTION_TITLE);
        formTitle.setForeground(UITheme.TEXT_DARK);
        card.add(formTitle, BorderLayout.NORTH);

        // Form Fields
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        fieldsPanel.setOpaque(false);

        // Field 1: Reg Number
        JPanel pnlReg = new JPanel(new BorderLayout(0, 5));
        pnlReg.setOpaque(false);
        JLabel lblReg = new JLabel("Vehicle Registration Number * (e.g., KA-05-AB-1234)");
        lblReg.setFont(UITheme.FONT_BOLD);
        lblReg.setForeground(UITheme.TEXT_DARK);
        txtRegNumber = new JTextField();
        txtRegNumber.setFont(UITheme.FONT_REGULAR);
        txtRegNumber.setPreferredSize(new Dimension(100, 38));
        pnlReg.add(lblReg, BorderLayout.NORTH);
        pnlReg.add(txtRegNumber, BorderLayout.CENTER);

        // Field 2: Owner Name
        JPanel pnlOwner = new JPanel(new BorderLayout(0, 5));
        pnlOwner.setOpaque(false);
        JLabel lblOwner = new JLabel("Owner Full Name *");
        lblOwner.setFont(UITheme.FONT_BOLD);
        lblOwner.setForeground(UITheme.TEXT_DARK);
        txtOwnerName = new JTextField();
        txtOwnerName.setFont(UITheme.FONT_REGULAR);
        txtOwnerName.setPreferredSize(new Dimension(100, 38));
        pnlOwner.add(lblOwner, BorderLayout.NORTH);
        pnlOwner.add(txtOwnerName, BorderLayout.CENTER);

        // Field 3: Vehicle Type
        JPanel pnlType = new JPanel(new BorderLayout(0, 5));
        pnlType.setOpaque(false);
        JLabel lblType = new JLabel("Vehicle Type Category *");
        lblType.setFont(UITheme.FONT_BOLD);
        lblType.setForeground(UITheme.TEXT_DARK);
        cmbVehicleType = new JComboBox<>(VehicleType.values());
        cmbVehicleType.setFont(UITheme.FONT_REGULAR);
        cmbVehicleType.setPreferredSize(new Dimension(100, 38));
        cmbVehicleType.addActionListener(e -> updateAvailability());
        pnlType.add(lblType, BorderLayout.NORTH);
        pnlType.add(cmbVehicleType, BorderLayout.CENTER);

        // Info Banner: Live slot count & Rate
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlInfo.setBackground(new Color(239, 246, 255));
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        lblSlotAvailabilityInfo = new JLabel("Available Slots: Checking...");
        lblSlotAvailabilityInfo.setFont(UITheme.FONT_BOLD);
        lblSlotAvailabilityInfo.setForeground(UITheme.PRIMARY_BLUE);

        lblHourlyRateInfo = new JLabel("Standard Rate: Rs. 0.00 / hour");
        lblHourlyRateInfo.setFont(UITheme.FONT_REGULAR);
        lblHourlyRateInfo.setForeground(UITheme.TEXT_DARK);

        pnlInfo.add(lblSlotAvailabilityInfo);
        pnlInfo.add(lblHourlyRateInfo);

        fieldsPanel.add(pnlReg);
        fieldsPanel.add(pnlOwner);
        fieldsPanel.add(pnlType);
        fieldsPanel.add(pnlInfo);

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(UITheme.FONT_BOLD);
        btnClear.setPreferredSize(new Dimension(100, 40));
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = UITheme.createButton("Park Vehicle & Issue Ticket", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnSubmit.setPreferredSize(new Dimension(230, 40));
        btnSubmit.addActionListener(e -> handleRegistration());

        btnPanel.add(btnClear);
        btnPanel.add(btnSubmit);

        card.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(card);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateAvailability() {
        VehicleType selectedType = (VehicleType) cmbVehicleType.getSelectedItem();
        if (selectedType == null) return;

        int available = manager.getSlotService().getAvailableCountByType(selectedType);
        int total = manager.getSlotService().getTotalSlotsCount(selectedType);
        double rate = manager.getTicketService().getFeeCalculator().getHourlyRate(selectedType);

        if (available > 0) {
            lblSlotAvailabilityInfo.setText("[AVAILABLE] Slots for " + selectedType.getDisplayName() + ": " + available + " of " + total + " free");
            lblSlotAvailabilityInfo.setForeground(UITheme.SUCCESS_GREEN);
        } else {
            lblSlotAvailabilityInfo.setText("[FULL] No slots available for " + selectedType.getDisplayName() + " (" + total + "/" + total + " occupied)");
            lblSlotAvailabilityInfo.setForeground(UITheme.DANGER_RED);
        }

        lblHourlyRateInfo.setText("Applicable Tariff: Rs. " + String.format("%.2f", rate) + " per hour (minimum 1 hr billing)");
    }

    private void handleRegistration() {
        String regNumber = txtRegNumber.getText().trim();
        String ownerName = txtOwnerName.getText().trim();
        VehicleType type = (VehicleType) cmbVehicleType.getSelectedItem();

        if (regNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the vehicle registration number.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            txtRegNumber.requestFocus();
            return;
        }

        if (ownerName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the vehicle owner's name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            txtOwnerName.requestFocus();
            return;
        }

        try {
            ParkingTicket ticket = manager.parkVehicle(regNumber, ownerName, type);
            clearForm();
            updateAvailability();

            // Display parking ticket receipt dialog
            showTicketReceiptDialog(ticket);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Blocked", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtRegNumber.setText("");
        txtOwnerName.setText("");
        cmbVehicleType.setSelectedIndex(0);
        txtRegNumber.requestFocus();
        updateAvailability();
    }

    private void showTicketReceiptDialog(ParkingTicket ticket) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Parking Ticket Receipt", true);
        dialog.setSize(420, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel head = new JPanel(new GridLayout(3, 1, 0, 3));
        head.setOpaque(false);
        JLabel title = new JLabel("PARKSMART FACILITY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("ENTRY PARKING TICKET", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sub.setForeground(UITheme.PRIMARY_BLUE);

        JLabel divider = new JLabel("========================================", SwingConstants.CENTER);
        divider.setForeground(UITheme.TEXT_MUTED);

        head.add(title);
        head.add(sub);
        head.add(divider);

        // Ticket Details
        JPanel body = new JPanel(new GridLayout(8, 2, 8, 8));
        body.setOpaque(false);

        addReceiptRow(body, "Ticket Number:", ticket.getTicketId());
        addReceiptRow(body, "Registration No:", ticket.getVehicle().getRegistrationNumber());
        addReceiptRow(body, "Owner Name:", ticket.getVehicle().getOwnerName());
        addReceiptRow(body, "Vehicle Type:", ticket.getVehicle().getVehicleType().getDisplayName());
        addReceiptRow(body, "Assigned Slot:", ticket.getSlotCode() + " (Slot #" + ticket.getSlotNumber() + ")");
        addReceiptRow(body, "Entry Timestamp:", ticket.getFormattedEntryTime());
        addReceiptRow(body, "Tariff Rate:", "Rs. " + String.format("%.2f", ticket.getHourlyRate()) + " / hr");
        addReceiptRow(body, "Current Status:", "ACTIVE / PARKED");

        // Slot Callout Highlight Box
        JPanel slotBox = new JPanel(new BorderLayout());
        slotBox.setBackground(new Color(236, 253, 245));
        slotBox.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS_GREEN, 2, true));
        slotBox.setPreferredSize(new Dimension(300, 55));

        JLabel lblSlotHighlight = new JLabel("PARK AT SLOT: " + ticket.getSlotCode(), SwingConstants.CENTER);
        lblSlotHighlight.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSlotHighlight.setForeground(new Color(6, 95, 70));
        slotBox.add(lblSlotHighlight, BorderLayout.CENTER);

        // Center assembly
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(body, BorderLayout.CENTER);
        centerWrapper.add(slotBox, BorderLayout.SOUTH);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);

        JButton btnClose = UITheme.createButton("Close & Continue", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnClose.addActionListener(e -> dialog.dispose());

        bottom.add(btnClose);

        panel.add(head, BorderLayout.NORTH);
        panel.add(centerWrapper, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void addReceiptRow(JPanel parent, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BOLD);
        val.setForeground(UITheme.TEXT_DARK);

        parent.add(lbl);
        parent.add(val);
    }
}
