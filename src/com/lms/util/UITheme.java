package com.lms.util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Modern Design System & Theme configuration for the LMS GUI Application.
 */
public class UITheme {

    // Color Palette (Tailored Slate & Indigo modern palette)
    public static final Color PRIMARY = new Color(79, 70, 229);        // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo 700
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);// Indigo 50

    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Slate 900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate 800
    public static final Color SIDEBAR_ACTIVE = new Color(49, 46, 129);  // Indigo 900
    public static final Color SIDEBAR_TEXT = new Color(226, 232, 240);  // Slate 200

    public static final Color BG_MAIN = new Color(248, 250, 252);      // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400

    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // Rose 500
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);
    public static final Color INFO = new Color(14, 165, 233);          // Sky 500
    public static final Color INFO_LIGHT = new Color(240, 249, 255);

    // Modern Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Initializes FlatLaf Light Look & Feel with modern UI defaults.
     */
    public static void setupFlatLaf() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", PRIMARY);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("TableHeader.font", FONT_HEADER);
            UIManager.put("TableHeader.background", new Color(241, 245, 249));
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
        } catch (Exception e) {
            System.err.println("FlatLaf initialization warning: " + e.getMessage());
        }
    }

    /**
     * Creates a sleek rounded card panel with soft border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return card;
    }

    /**
     * Creates a modern styled primary button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Creates a secondary outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        return btn;
    }

    /**
     * Creates a danger button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Creates a badge label with background and text color.
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(FONT_BADGE);
        badge.setForeground(fg);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
