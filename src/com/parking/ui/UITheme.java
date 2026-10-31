package com.parking.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized theme colors, typography, and utility UI components.
 * Provides a clean, modern dashboard aesthetic.
 */
public class UITheme {

    // Palette Colors
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);          // Deep slate #0F172A
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);       // Slate #1E293B
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);      // Indigo blue #2563EB
    public static final Color SIDEBAR_TEXT = new Color(241, 245, 249);
    public static final Color SIDEBAR_MUTED = new Color(148, 163, 184);

    public static final Color CONTENT_BG = new Color(248, 250, 252);       // Clean light background #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);      // Slate 200

    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    public static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216);

    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);      // Emerald #10B981
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);

    public static final Color DANGER_RED = new Color(239, 68, 68);          // Red #EF4444
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);

    public static final Color WARNING_AMBER = new Color(245, 158, 11);      // Amber #F59E0B
    public static final Color WARNING_LIGHT = new Color(254, 252, 232);

    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // Typography
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Creates a standardized card panel with white background, rounded border, and padding.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }

    /**
     * Creates a custom styled JButton with hover effect.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (bg.equals(PRIMARY_BLUE)) {
                    btn.setBackground(PRIMARY_BLUE_HOVER);
                } else if (bg.equals(SUCCESS_GREEN)) {
                    btn.setBackground(new Color(5, 150, 105));
                } else if (bg.equals(DANGER_RED)) {
                    btn.setBackground(new Color(220, 38, 38));
                } else {
                    btn.setBackground(bg.darker());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    /**
     * Applies modern styling to JTable (header, row heights, alternating row colors, padding).
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);
    }
}
