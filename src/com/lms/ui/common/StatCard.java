package com.lms.ui.common;

import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Modern KPI Stat Card widget for Dashboards.
 */
public class StatCard extends JPanel {

    private final JLabel lblValue;
    private final JLabel lblTitle;
    private final JLabel lblSubtitle;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setPreferredSize(new Dimension(200, 105));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Accent indicator dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(2, 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);
        headerPanel.add(dot, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_TITLE);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);
        add(lblValue, BorderLayout.CENTER);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }
}
