package com.lms.ui.admin;

import com.lms.concurrent.SystemActivityMonitor;
import com.lms.db.AuditLogDAO;
import com.lms.model.AuditLog;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class SystemActivityPanel extends JPanel implements SystemActivityMonitor.ActivityListener {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableLogs;
    private final DefaultTableModel tableModel;
    private final JLabel lblStatus;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbActionFilter;

    private List<AuditLog> currentLogs = new ArrayList<>();

    public SystemActivityPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Real-Time System Activity & Audit Trail");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusPanel.setOpaque(false);

        lblStatus = UITheme.createBadge("● LIVE MONITORING ACTIVE", UITheme.SUCCESS_LIGHT, UITheme.SUCCESS);
        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh Now");
        btnRefresh.addActionListener(e -> reloadLogs());

        statusPanel.add(lblStatus);
        statusPanel.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(statusPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));

        filterCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(18);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search details, user, action...");
        txtSearch.addCaretListener(e -> filterLogs());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Action Filter:"));
        cmbActionFilter = new JComboBox<>(new String[]{
                "All Actions",
                "USER_CREATED",
                "USER_UPDATED",
                "USER_DELETED",
                "COURSE_CREATED",
                "ENROLLMENT",
                "ASSIGNMENT_SUBMISSION",
                "ASSIGNMENT_GRADED",
                "SETTINGS_UPDATED"
        });
        cmbActionFilter.addActionListener(e -> filterLogs());
        filterCard.add(cmbActionFilter);

        // Table
        String[] columns = {"Log ID", "Timestamp", "User", "Action Triggered", "Activity Details", "Client IP"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLogs = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableLogs);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Register to multithreaded monitor
        SystemActivityMonitor.getInstance().addListener(this);
        reloadLogs();
    }

    public void reloadLogs() {
        try {
            currentLogs = auditLogDAO.getRecentLogs(100);
            filterLogs();
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load audit logs: " + e.getMessage());
        }
    }

    private void filterLogs() {
        String query = txtSearch.getText().trim().toLowerCase();
        String actionFilter = (String) cmbActionFilter.getSelectedItem();

        tableModel.setRowCount(0);
        for (AuditLog log : currentLogs) {
            boolean matchesSearch = query.isEmpty() ||
                    log.getUserName().toLowerCase().contains(query) ||
                    log.getAction().toLowerCase().contains(query) ||
                    log.getDetails().toLowerCase().contains(query);

            boolean matchesAction = actionFilter == null ||
                    actionFilter.equals("All Actions") ||
                    log.getAction().equalsIgnoreCase(actionFilter);

            if (matchesSearch && matchesAction) {
                tableModel.addRow(new Object[]{
                        log.getId(),
                        log.getFormattedTimestamp(),
                        log.getUserName(),
                        log.getAction(),
                        log.getDetails(),
                        log.getIpAddress()
                });
            }
        }
    }

    @Override
    public void onNewActivities(List<AuditLog> recentLogs) {
        this.currentLogs = recentLogs;
        filterLogs();
    }
}
