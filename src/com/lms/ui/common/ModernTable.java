package com.lms.ui.common;

import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;

/**
 * Modern JTable component with customized badge renderers and alternating rows.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(38);
        setFont(UITheme.FONT_BODY);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionBackground(UITheme.PRIMARY_LIGHT);
        setSelectionForeground(UITheme.PRIMARY);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setFont(UITheme.FONT_HEADER);
        getTableHeader().setBackground(new Color(248, 250, 252));
        getTableHeader().setForeground(UITheme.TEXT_PRIMARY);

        setDefaultRenderer(Object.class, new ModernCellRenderer());
    }

    public static class ModernCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                c.setForeground(UITheme.TEXT_PRIMARY);
            }

            if (value != null) {
                String str = value.toString().trim().toUpperCase();
                if (str.equals("ACTIVE") || str.equals("COMPLETED") || str.equals("GRADED")) {
                    JLabel badge = UITheme.createBadge(value.toString(), UITheme.SUCCESS_LIGHT, UITheme.SUCCESS);
                    badge.setHorizontalAlignment(LEFT);
                    return badge;
                } else if (str.equals("PENDING") || str.equals("DRAFT") || str.equals("IN PROGRESS")) {
                    JLabel badge = UITheme.createBadge(value.toString(), UITheme.WARNING_LIGHT, UITheme.WARNING);
                    badge.setHorizontalAlignment(LEFT);
                    return badge;
                } else if (str.equals("SUSPENDED") || str.equals("INACTIVE") || str.equals("DROPPED") || str.equals("FAILED")) {
                    JLabel badge = UITheme.createBadge(value.toString(), UITheme.DANGER_LIGHT, UITheme.DANGER);
                    badge.setHorizontalAlignment(LEFT);
                    return badge;
                } else if (str.equals("ADMIN") || str.equals("INSTRUCTOR") || str.equals("STUDENT")) {
                    Color bg = str.equals("ADMIN") ? UITheme.DANGER_LIGHT : (str.equals("INSTRUCTOR") ? UITheme.INFO_LIGHT : UITheme.PRIMARY_LIGHT);
                    Color fg = str.equals("ADMIN") ? UITheme.DANGER : (str.equals("INSTRUCTOR") ? UITheme.INFO : UITheme.PRIMARY);
                    JLabel badge = UITheme.createBadge(value.toString(), bg, fg);
                    badge.setHorizontalAlignment(LEFT);
                    return badge;
                }
            }

            return c;
        }
    }
}
