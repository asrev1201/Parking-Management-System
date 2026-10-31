package com.parking.ui;

import com.parking.model.ParkingTicket;
import com.parking.model.VehicleType;
import com.parking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Historical records and audit trail panel.
 * Displays all active and completed vehicle parking sessions with
 * responsive search, multi-criteria filtering, and detailed ticket inspection.
 */
public class RecordsPanel extends JPanel {
    private final ParkingManager manager;
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable recordsTable;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JComboBox<String> cmbTypeFilter;
    private JLabel lblTotalCount;

    public RecordsPanel(ParkingManager manager, MainFrame mainFrame) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initUI();
        refreshRecords();
    }

    private void initUI() {
        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Vehicle Parking Records & Audit Logs");
        title.setFont(UITheme.FONT_HEADER_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Complete log of all ongoing and past vehicle parking sessions");
        subtitle.setFont(UITheme.FONT_REGULAR);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // 2. Filter & Actions Toolbar Card (2-row stacked layout so buttons never overlap)
        JPanel toolbarCard = UITheme.createCardPanel();
        toolbarCard.setLayout(new BorderLayout(0, 12));

        // Row 1: Search and Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(150, 32));
        txtSearch.setToolTipText("Search by ticket, plate number, owner, or slot");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "Active (Parked)", "Completed (Exited)"});
        cmbStatusFilter.setFont(UITheme.FONT_REGULAR);
        cmbStatusFilter.setPreferredSize(new Dimension(160, 32));
        cmbStatusFilter.addActionListener(e -> applyFilters());

        JLabel lblType = new JLabel("Vehicle Type:");
        lblType.setFont(UITheme.FONT_BOLD);
        lblType.setForeground(UITheme.TEXT_DARK);

        cmbTypeFilter = new JComboBox<>(new String[]{"All Types", "Bike", "Car", "Bus"});
        cmbTypeFilter.setFont(UITheme.FONT_REGULAR);
        cmbTypeFilter.setPreferredSize(new Dimension(110, 32));
        cmbTypeFilter.addActionListener(e -> applyFilters());

        JButton btnReset = new JButton("Reset Filters");
        btnReset.setFont(UITheme.FONT_REGULAR);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(105, 32));
        btnReset.addActionListener(e -> resetFilters());

        filterRow.add(lblSearch);
        filterRow.add(txtSearch);
        filterRow.add(lblStatus);
        filterRow.add(cmbStatusFilter);
        filterRow.add(lblType);
        filterRow.add(cmbTypeFilter);
        filterRow.add(btnReset);

        // Row 2: Record Counters & Action Buttons
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);

        lblTotalCount = new JLabel("Showing: 0 records");
        lblTotalCount.setFont(UITheme.FONT_BOLD);
        lblTotalCount.setForeground(UITheme.TEXT_MUTED);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(UITheme.FONT_BOLD);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(90, 34));
        btnRefresh.addActionListener(e -> refreshRecords());

        JButton btnViewDetails = UITheme.createButton("View Ticket Details", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnViewDetails.setPreferredSize(new Dimension(160, 34));
        btnViewDetails.addActionListener(e -> viewSelectedTicket());

        buttonGroup.add(btnRefresh);
        buttonGroup.add(btnViewDetails);

        actionRow.add(lblTotalCount, BorderLayout.WEST);
        actionRow.add(buttonGroup, BorderLayout.EAST);

        toolbarCard.add(filterRow, BorderLayout.NORTH);
        toolbarCard.add(actionRow, BorderLayout.SOUTH);

        // 3. Records Table Setup
        String[] columns = {
                "Ticket ID", "Registration No", "Owner Name", "Type",
                "Slot", "Entry Time", "Exit Time", "Duration", "Fee (Rs.)", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recordsTable = new JTable(tableModel);
        UITheme.styleTable(recordsTable);

        // Adjust column widths for optimal reading
        TableColumnModel colModel = recordsTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(85);   // Ticket ID
        colModel.getColumn(1).setPreferredWidth(125);  // Reg No
        colModel.getColumn(2).setPreferredWidth(125);  // Owner Name
        colModel.getColumn(3).setPreferredWidth(65);   // Type
        colModel.getColumn(4).setPreferredWidth(65);   // Slot
        colModel.getColumn(5).setPreferredWidth(135);  // Entry Time
        colModel.getColumn(6).setPreferredWidth(135);  // Exit Time
        colModel.getColumn(7).setPreferredWidth(75);   // Duration
        colModel.getColumn(8).setPreferredWidth(75);   // Fee
        colModel.getColumn(9).setPreferredWidth(95);   // Status

        // Custom renderer for Status column (Active vs Completed)
        recordsTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                if ("ACTIVE".equalsIgnoreCase(val)) {
                    setForeground(new Color(5, 150, 105));
                    setFont(UITheme.FONT_BOLD);
                } else if ("COMPLETED".equalsIgnoreCase(val)) {
                    setForeground(new Color(100, 116, 139));
                    setFont(UITheme.FONT_REGULAR);
                }
                if (isSelected) {
                    setBackground(new Color(238, 242, 255));
                } else {
                    setBackground(Color.WHITE);
                }
                return c;
            }
        });

        // Double click on row to open ticket details
        recordsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && recordsTable.getSelectedRow() != -1) {
                    viewSelectedTicket();
                }
            }
        });

        rowSorter = new TableRowSorter<>(tableModel);
        recordsTable.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(recordsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(toolbarCard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshRecords() {
        tableModel.setRowCount(0);
        List<ParkingTicket> allTickets = manager.getAllTickets();

        for (ParkingTicket t : allTickets) {
            tableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getRegistrationNumber(),
                    t.getVehicle().getOwnerName(),
                    t.getVehicle().getVehicleType().getDisplayName(),
                    t.getSlotCode(),
                    t.getFormattedEntryTime(),
                    t.getFormattedExitTime() != null && !t.getFormattedExitTime().isEmpty() ? t.getFormattedExitTime() : "-",
                    t.getFormattedDuration(),
                    String.format("%.2f", t.getTotalFee()),
                    t.getStatus()
            });
        }

        applyFilters();
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbStatusFilter.setSelectedIndex(0);
        cmbTypeFilter.setSelectedIndex(0);
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().trim();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();
        String typeFilter = (String) cmbTypeFilter.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText)));
        }

        if (statusFilter != null) {
            if (statusFilter.contains("Active")) {
                filters.add(RowFilter.regexFilter("^ACTIVE$", 9));
            } else if (statusFilter.contains("Completed")) {
                filters.add(RowFilter.regexFilter("^COMPLETED$", 9));
            }
        }

        if (typeFilter != null && !typeFilter.equals("All Types")) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(typeFilter) + "$", 3));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        lblTotalCount.setText("Showing: " + recordsTable.getRowCount() + " of " + tableModel.getRowCount() + " records");
    }

    private void viewSelectedTicket() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a record from the table to view its details.",
                    "No Record Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = recordsTable.convertRowIndexToModel(selectedRow);
        String ticketId = (String) tableModel.getValueAt(modelRow, 0);

        ParkingTicket ticket = null;
        for (ParkingTicket t : manager.getAllTickets()) {
            if (t.getTicketId().equalsIgnoreCase(ticketId)) {
                ticket = t;
                break;
            }
        }

        if (ticket != null) {
            showTicketModalDialog(ticket);
        }
    }

    private void showTicketModalDialog(ParkingTicket ticket) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ticket Details - " + ticket.getTicketId(), true);
        dialog.setSize(440, 530);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel head = new JPanel(new GridLayout(3, 1, 0, 3));
        head.setOpaque(false);
        JLabel title = new JLabel("PARKSMART FACILITY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("PARKING SESSION RECORD", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sub.setForeground(UITheme.PRIMARY_BLUE);

        JLabel divider = new JLabel("----------------------------------------------------------------", SwingConstants.CENTER);
        divider.setForeground(UITheme.BORDER_COLOR);

        head.add(title);
        head.add(sub);
        head.add(divider);

        // Body Details
        JPanel body = new JPanel(new GridLayout(10, 2, 8, 6));
        body.setOpaque(false);

        addDetailRow(body, "Ticket Number:", ticket.getTicketId());
        addDetailRow(body, "Registration No:", ticket.getVehicle().getRegistrationNumber());
        addDetailRow(body, "Owner Name:", ticket.getVehicle().getOwnerName());
        addDetailRow(body, "Vehicle Type:", ticket.getVehicle().getVehicleType().getDisplayName());
        addDetailRow(body, "Assigned Slot:", ticket.getSlotCode() + " (Slot #" + ticket.getSlotNumber() + ")");
        addDetailRow(body, "Entry Time:", ticket.getFormattedEntryTime());
        addDetailRow(body, "Exit Time:", ticket.getFormattedExitTime() != null && !ticket.getFormattedExitTime().isEmpty() ? ticket.getFormattedExitTime() : "Still Parked");
        addDetailRow(body, "Total Duration:", ticket.getFormattedDuration());
        addDetailRow(body, "Hourly Tariff:", "Rs. " + String.format("%.2f", ticket.getHourlyRate()) + " / hr");
        addDetailRow(body, "Total Fee Paid:", "Rs. " + String.format("%.2f", ticket.getTotalFee()));

        // Status Banner
        boolean isActive = ticket.isActive();
        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setBackground(isActive ? new Color(236, 253, 245) : new Color(241, 245, 249));
        statusBox.setBorder(BorderFactory.createLineBorder(isActive ? UITheme.SUCCESS_GREEN : new Color(148, 163, 184), 1, true));
        statusBox.setPreferredSize(new Dimension(300, 42));

        JLabel lblStatusBanner = new JLabel(isActive ? "CURRENT STATUS: ACTIVE (PARKED)" : "CURRENT STATUS: COMPLETED (SETTLED)", SwingConstants.CENTER);
        lblStatusBanner.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusBanner.setForeground(isActive ? new Color(6, 95, 70) : new Color(51, 65, 85));
        statusBox.add(lblStatusBanner, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(body, BorderLayout.CENTER);
        centerWrapper.add(statusBox, BorderLayout.SOUTH);

        // Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);

        JButton btnClose = UITheme.createButton("Close", UITheme.PRIMARY_BLUE, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(120, 36));
        btnClose.addActionListener(e -> dialog.dispose());
        bottom.add(btnClose);

        panel.add(head, BorderLayout.NORTH);
        panel.add(centerWrapper, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel parent, String label, String value) {
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
