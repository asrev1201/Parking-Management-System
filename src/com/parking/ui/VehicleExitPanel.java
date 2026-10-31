package com.parking.ui;

import com.parking.model.ParkingTicket;
import com.parking.service.ParkingFeeCalculator;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Handles vehicle checkout, search by Ticket ID or Registration Number,
 * real-time fee computation, itemized invoice generation, and slot release.
 */
public class VehicleExitPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private JTextField txtSearchQuery;
    private DefaultTableModel activeVehiclesModel;
    private JTable activeTable;

    // Retrieved details labels
    private JPanel detailsCard;
    private JLabel lblTicketIdVal;
    private JLabel lblRegNoVal;
    private JLabel lblOwnerVal;
    private JLabel lblTypeVal;
    private JLabel lblSlotVal;
    private JLabel lblEntryTimeVal;
    private JLabel lblDurationVal;
    private JLabel lblRateVal;
    private JLabel lblTotalFeeVal;

    private JButton btnProcessExit;
    private ParkingTicket currentSelectedTicket;

    public VehicleExitPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        refreshActiveVehicles();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Vehicle Exit & Fee Settlement");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Locate an active parked vehicle, compute duration & tariff, and issue the departure bill");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // Top Search Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new BorderLayout(15, 10));

        JLabel searchLabel = new JLabel("Search Parked Vehicle (by Ticket ID or Registration Number):");
        searchLabel.setFont(UITheme.FONT_BOLD);
        searchLabel.setForeground(UITheme.TEXT_DARK);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        txtSearchQuery = new JTextField();
        txtSearchQuery.setFont(UITheme.FONT_REGULAR);
        txtSearchQuery.setPreferredSize(new Dimension(200, 38));
        txtSearchQuery.addActionListener(e -> searchVehicle());

        JButton btnSearch = UITheme.createButton("Find Vehicle", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(150, 38));
        btnSearch.addActionListener(e -> searchVehicle());

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(UITheme.FONT_BOLD);
        btnClear.setPreferredSize(new Dimension(80, 38));
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearSearch());

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBtns.setOpaque(false);
        searchBtns.add(btnClear);
        searchBtns.add(btnSearch);

        searchRow.add(txtSearchQuery, BorderLayout.CENTER);
        searchRow.add(searchBtns, BorderLayout.EAST);

        searchCard.add(searchLabel, BorderLayout.NORTH);
        searchCard.add(searchRow, BorderLayout.CENTER);

        // Middle Split Section: Left = Active Vehicles quick selector, Right = Retrieved details & checkout
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        splitPanel.setOpaque(false);

        // Left Card: Active Vehicles Quick Pick Table
        JPanel leftCard = UITheme.createCardPanel();
        leftCard.setLayout(new BorderLayout(0, 10));

        JLabel leftTitle = new JLabel("Currently Parked Vehicles (Click row to select)");
        leftTitle.setFont(UITheme.FONT_SECTION_TITLE);
        leftTitle.setForeground(UITheme.TEXT_DARK);
        leftCard.add(leftTitle, BorderLayout.NORTH);

        String[] cols = {"Ticket ID", "Registration No", "Type", "Slot", "Parked Since"};
        activeVehiclesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        activeTable = new JTable(activeVehiclesModel);
        UITheme.styleTable(activeTable);
        activeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && activeTable.getSelectedRow() != -1) {
                String ticketId = (String) activeTable.getValueAt(activeTable.getSelectedRow(), 0);
                txtSearchQuery.setText(ticketId);
                searchVehicle();
            }
        });

        JScrollPane scrollTable = new JScrollPane(activeTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        leftCard.add(scrollTable, BorderLayout.CENTER);

        // Right Card: Vehicle Details & Checkout Invoice
        detailsCard = UITheme.createCardPanel();
        detailsCard.setLayout(new BorderLayout(0, 15));

        JLabel rightTitle = new JLabel("Exit Billing & Payment Calculation");
        rightTitle.setFont(UITheme.FONT_SECTION_TITLE);
        rightTitle.setForeground(UITheme.TEXT_DARK);
        detailsCard.add(rightTitle, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridLayout(9, 2, 8, 8));
        detailsGrid.setOpaque(false);

        lblTicketIdVal = createDetailLabel("--");
        lblRegNoVal = createDetailLabel("--");
        lblOwnerVal = createDetailLabel("--");
        lblTypeVal = createDetailLabel("--");
        lblSlotVal = createDetailLabel("--");
        lblEntryTimeVal = createDetailLabel("--");
        lblDurationVal = createDetailLabel("--");
        lblRateVal = createDetailLabel("--");
        lblTotalFeeVal = createDetailLabel("--");
        lblTotalFeeVal.setForeground(new Color(220, 38, 38));
        lblTotalFeeVal.setFont(new Font("Segoe UI", Font.BOLD, 16));

        addDetailRow(detailsGrid, "Ticket ID:", lblTicketIdVal);
        addDetailRow(detailsGrid, "Registration Number:", lblRegNoVal);
        addDetailRow(detailsGrid, "Vehicle Owner:", lblOwnerVal);
        addDetailRow(detailsGrid, "Vehicle Category:", lblTypeVal);
        addDetailRow(detailsGrid, "Occupied Slot:", lblSlotVal);
        addDetailRow(detailsGrid, "Entry Time:", lblEntryTimeVal);
        addDetailRow(detailsGrid, "Calculated Duration:", lblDurationVal);
        addDetailRow(detailsGrid, "Applicable Tariff:", lblRateVal);
        addDetailRow(detailsGrid, "Total Amount Due:", lblTotalFeeVal);

        detailsCard.add(detailsGrid, BorderLayout.CENTER);

        // Checkout Button Panel
        JPanel checkoutBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        checkoutBtnPanel.setOpaque(false);

        btnProcessExit = UITheme.createButton("Process Exit & Release Slot", UITheme.DANGER_RED, Color.WHITE);
        btnProcessExit.setPreferredSize(new Dimension(250, 42));
        btnProcessExit.setEnabled(false);
        btnProcessExit.addActionListener(e -> processExit());

        checkoutBtnPanel.add(btnProcessExit);
        detailsCard.add(checkoutBtnPanel, BorderLayout.SOUTH);

        splitPanel.add(leftCard);
        splitPanel.add(detailsCard);

        // Center assembly
        JPanel centerContainer = new JPanel(new BorderLayout(0, 15));
        centerContainer.setOpaque(false);
        centerContainer.add(searchCard, BorderLayout.NORTH);
        centerContainer.add(splitPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JLabel createDetailLabel(String initialText) {
        JLabel lbl = new JLabel(initialText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_DARK);
        return lbl;
    }

    private void addDetailRow(JPanel parent, String label, JLabel valLabel) {
        JLabel title = new JLabel(label);
        title.setFont(UITheme.FONT_REGULAR);
        title.setForeground(UITheme.TEXT_MUTED);
        parent.add(title);
        parent.add(valLabel);
    }

    private void searchVehicle() {
        String query = txtSearchQuery.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Ticket ID or Vehicle Registration Number.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            txtSearchQuery.requestFocus();
            return;
        }

        ParkingTicket ticket = manager.findActiveTicket(query);
        if (ticket == null) {
            // Check completed
            ParkingTicket completed = manager.findCompletedTicket(query);
            if (completed != null) {
                JOptionPane.showMessageDialog(this,
                        "Ticket " + completed.getTicketId() + " (" + completed.getVehicle().getRegistrationNumber() 
                        + ") has already exited and settled on " + completed.getFormattedExitTime() 
                        + " with fee " + completed.getFormattedFee() + ".",
                        "Already Settled", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No currently parked vehicle found matching '" + query + "'.",
                        "Vehicle Not Found", JOptionPane.ERROR_MESSAGE);
            }
            clearSelectedDetails();
            return;
        }

        displayTicketForCheckout(ticket);
    }

    private void displayTicketForCheckout(ParkingTicket ticket) {
        this.currentSelectedTicket = ticket;
        LocalDateTime now = LocalDateTime.now();
        ParkingFeeCalculator calc = manager.getTicketService().getFeeCalculator();

        long billableHours = calc.calculateBillableHours(ticket.getEntryTime(), now);
        double fee = calc.calculateFee(ticket.getVehicle().getVehicleType(), ticket.getEntryTime(), now);
        String exactDuration = calc.getExactDurationString(ticket.getEntryTime(), now);

        lblTicketIdVal.setText(ticket.getTicketId());
        lblRegNoVal.setText(ticket.getVehicle().getRegistrationNumber());
        lblOwnerVal.setText(ticket.getVehicle().getOwnerName());
        lblTypeVal.setText(ticket.getVehicle().getVehicleType().getDisplayName());
        lblSlotVal.setText(ticket.getSlotCode() + " (#" + ticket.getSlotNumber() + ")");
        lblEntryTimeVal.setText(ticket.getFormattedEntryTime());
        lblDurationVal.setText(billableHours + " hour(s) [Exact: " + exactDuration + "]");
        lblRateVal.setText("Rs. " + String.format("%.2f", ticket.getHourlyRate()) + " / hr");
        lblTotalFeeVal.setText(String.format("Rs. %.2f", fee));

        btnProcessExit.setEnabled(true);
    }

    private void processExit() {
        if (currentSelectedTicket == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm payment settlement and slot release for Vehicle:\n" +
                "Registration: " + currentSelectedTicket.getVehicle().getRegistrationNumber() + "\n" +
                "Ticket ID: " + currentSelectedTicket.getTicketId() + "\n" +
                "Slot: " + currentSelectedTicket.getSlotCode(),
                "Confirm Exit & Payment",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ParkingTicket completedTicket = manager.exitVehicle(currentSelectedTicket.getTicketId());

                // Show Itemized Bill Dialog
                showItemizedBillDialog(completedTicket);

                clearSearch();
                refreshActiveVehicles();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error processing exit: " + ex.getMessage(),
                        "Exit Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearSearch() {
        txtSearchQuery.setText("");
        clearSelectedDetails();
        activeTable.clearSelection();
    }

    private void clearSelectedDetails() {
        currentSelectedTicket = null;
        lblTicketIdVal.setText("--");
        lblRegNoVal.setText("--");
        lblOwnerVal.setText("--");
        lblTypeVal.setText("--");
        lblSlotVal.setText("--");
        lblEntryTimeVal.setText("--");
        lblDurationVal.setText("--");
        lblRateVal.setText("--");
        lblTotalFeeVal.setText("--");
        btnProcessExit.setEnabled(false);
    }

    public void refreshActiveVehicles() {
        activeVehiclesModel.setRowCount(0);
        List<ParkingTicket> active = manager.getActiveTickets();
        for (ParkingTicket t : active) {
            activeVehiclesModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getRegistrationNumber(),
                    t.getVehicle().getVehicleType().getDisplayName(),
                    t.getSlotCode(),
                    t.getFormattedEntryTime()
            });
        }
    }

    private void showItemizedBillDialog(ParkingTicket ticket) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Itemized Parking Invoice", true);
        dialog.setSize(460, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Invoice Header
        JPanel head = new JPanel(new GridLayout(4, 1, 0, 2));
        head.setOpaque(false);
        JLabel title = new JLabel("PARKSMART MANAGEMENT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("FINAL PARKING TAX INVOICE", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sub.setForeground(UITheme.PRIMARY_BLUE);

        JLabel addr = new JLabel("Main Gate Terminal | City Center", SwingConstants.CENTER);
        addr.setFont(UITheme.FONT_BADGE);
        addr.setForeground(UITheme.TEXT_MUTED);

        JLabel div = new JLabel("-------------------------------------------------------------------------", SwingConstants.CENTER);
        div.setForeground(UITheme.BORDER_COLOR);

        head.add(title);
        head.add(sub);
        head.add(addr);
        head.add(div);

        // Body Details
        JPanel body = new JPanel(new GridLayout(9, 2, 8, 8));
        body.setOpaque(false);

        addBillRow(body, "Invoice / Ticket #:", ticket.getTicketId());
        addBillRow(body, "Registration No:", ticket.getVehicle().getRegistrationNumber());
        addBillRow(body, "Owner Name:", ticket.getVehicle().getOwnerName());
        addBillRow(body, "Vehicle Type:", ticket.getVehicle().getVehicleType().getDisplayName());
        addBillRow(body, "Vacated Slot:", ticket.getSlotCode() + " (#" + ticket.getSlotNumber() + ")");
        addBillRow(body, "Entry Time:", ticket.getFormattedEntryTime());
        addBillRow(body, "Exit Time:", ticket.getFormattedExitTime());
        addBillRow(body, "Billed Duration:", ticket.getDurationHours() + " hour(s)");
        addBillRow(body, "Hourly Tariff:", "Rs. " + String.format("%.2f", ticket.getHourlyRate()) + " / hr");

        // Total Due Highlight Panel
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(new Color(254, 242, 242));
        totalBox.setBorder(BorderFactory.createLineBorder(UITheme.DANGER_RED, 2, true));
        totalBox.setPreferredSize(new Dimension(300, 60));

        JLabel lblPaidBanner = new JLabel("TOTAL PAID: " + String.format("Rs. %.2f", ticket.getTotalFee()), SwingConstants.CENTER);
        lblPaidBanner.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPaidBanner.setForeground(new Color(185, 28, 28));
        totalBox.add(lblPaidBanner, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(body, BorderLayout.CENTER);
        centerWrapper.add(totalBox, BorderLayout.SOUTH);

        // Footer
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);

        JButton btnClose = UITheme.createButton("Print / Close Invoice", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnClose.addActionListener(e -> dialog.dispose());

        bottom.add(btnClose);

        panel.add(head, BorderLayout.NORTH);
        panel.add(centerWrapper, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void addBillRow(JPanel parent, String label, String value) {
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
