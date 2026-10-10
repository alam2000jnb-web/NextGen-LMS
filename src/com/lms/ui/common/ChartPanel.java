package com.lms.ui.common;

import com.lms.util.UITheme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Java 2D Graphics Chart component for Performance Analytics
 * and Enrollment Stats (Visualizations requirement in rubric).
 */
public class ChartPanel extends JPanel {

    public enum ChartType { BAR, DONUT }

    private ChartType chartType = ChartType.BAR;
    private String chartTitle = "Performance Chart";
    private final Map<String, Double> dataPoints = new LinkedHashMap<>();

    private static final Color[] PALETTE = {
            new Color(99, 102, 241),  // Indigo
            new Color(16, 185, 129),  // Emerald
            new Color(245, 158, 11),  // Amber
            new Color(239, 68, 68),   // Rose
            new Color(14, 165, 233),  // Sky
            new Color(168, 85, 247)   // Purple
    };

    public ChartPanel(String title, ChartType type) {
        this.chartTitle = title;
        this.chartType = type;
        setOpaque(false);
        setPreferredSize(new Dimension(380, 260));
    }

    public void setData(Map<String, ? extends Number> data) {
        dataPoints.clear();
        if (data != null) {
            for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
                dataPoints.put(entry.getKey(), entry.getValue() != null ? entry.getValue().doubleValue() : 0.0);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card background
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, w, h, 14, 14);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Title
        g2.setFont(UITheme.FONT_HEADER);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString(chartTitle, 16, 28);

        if (dataPoints.isEmpty()) {
            g2.setFont(UITheme.FONT_BODY);
            g2.setColor(UITheme.TEXT_MUTED);
            String emptyMsg = "No data available to display";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(emptyMsg, (w - fm.stringWidth(emptyMsg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        if (chartType == ChartType.BAR) {
            paintBarChart(g2, w, h);
        } else {
            paintDonutChart(g2, w, h);
        }

        g2.dispose();
    }

    private void paintBarChart(Graphics2D g2, int w, int h) {
        int paddingLeft = 40;
        int paddingRight = 20;
        int paddingTop = 45;
        int paddingBottom = 45;

        int plotWidth = w - paddingLeft - paddingRight;
        int plotHeight = h - paddingTop - paddingBottom;

        double maxVal = 1.0;
        for (double val : dataPoints.values()) {
            if (val > maxVal) maxVal = val;
        }

        int count = dataPoints.size();
        int barWidth = Math.max(16, Math.min(48, plotWidth / (count * 2)));
        int gap = (plotWidth - (barWidth * count)) / (count + 1);

        // Draw horizontal grid lines
        g2.setColor(new Color(241, 245, 249));
        g2.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i <= 4; i++) {
            int y = paddingTop + (int) (plotHeight * (i / 4.0));
            g2.drawLine(paddingLeft, y, w - paddingRight, y);
        }

        int x = paddingLeft + gap;
        int colorIdx = 0;

        for (Map.Entry<String, Double> entry : dataPoints.entrySet()) {
            double val = entry.getValue();
            int barHeight = (int) ((val / maxVal) * (plotHeight - 10));
            int y = paddingTop + plotHeight - barHeight;

            Color barColor = PALETTE[colorIdx % PALETTE.length];
            GradientPaint gp = new GradientPaint(x, y, barColor, x, y + barHeight, barColor.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

            // Value label above bar
            g2.setFont(UITheme.FONT_SMALL);
            g2.setColor(UITheme.TEXT_PRIMARY);
            String valStr = val == Math.floor(val) ? String.format("%.0f", val) : String.format("%.1f", val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 4);

            // Category label below bar
            String key = entry.getKey();
            if (key.length() > 8) key = key.substring(0, 7) + "…";
            g2.setColor(UITheme.TEXT_SECONDARY);
            g2.drawString(key, x + (barWidth - fm.stringWidth(key)) / 2, paddingTop + plotHeight + 16);

            x += barWidth + gap;
            colorIdx++;
        }
    }

    private void paintDonutChart(Graphics2D g2, int w, int h) {
        int diameter = Math.min(w / 2, h - 80);
        int cx = 30;
        int cy = 45 + (h - 45 - diameter) / 2;

        double total = 0;
        for (double val : dataPoints.values()) total += val;
        if (total <= 0) total = 1.0;

        double curAngle = 90;
        int colorIdx = 0;

        for (Map.Entry<String, Double> entry : dataPoints.entrySet()) {
            double angle = (entry.getValue() / total) * 360.0;
            g2.setColor(PALETTE[colorIdx % PALETTE.length]);
            g2.fillArc(cx, cy, diameter, diameter, (int) Math.round(curAngle), (int) Math.round(angle));
            curAngle += angle;
            colorIdx++;
        }

        // Inner circle to make donut
        int innerDiameter = (int) (diameter * 0.58);
        int innerOffset = (diameter - innerDiameter) / 2;
        g2.setColor(UITheme.CARD_BG);
        g2.fillOval(cx + innerOffset, cy + innerOffset, innerDiameter, innerDiameter);

        // Center total
        g2.setFont(UITheme.FONT_SUBTITLE);
        g2.setColor(UITheme.TEXT_PRIMARY);
        String totStr = String.format("%.0f", total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totStr, cx + (diameter - fm.stringWidth(totStr)) / 2, cy + (diameter / 2) + 6);

        // Legend on right side
        int legX = cx + diameter + 25;
        int legY = 65;
        colorIdx = 0;
        g2.setFont(UITheme.FONT_SMALL);

        for (Map.Entry<String, Double> entry : dataPoints.entrySet()) {
            g2.setColor(PALETTE[colorIdx % PALETTE.length]);
            g2.fillRoundRect(legX, legY - 9, 10, 10, 3, 3);

            g2.setColor(UITheme.TEXT_PRIMARY);
            String label = entry.getKey();
            if (label.length() > 14) label = label.substring(0, 13) + "…";
            String pct = String.format(" (%.0f%%)", (entry.getValue() / total) * 100);
            g2.drawString(label + pct, legX + 16, legY);

            legY += 22;
            colorIdx++;
        }
    }
}
