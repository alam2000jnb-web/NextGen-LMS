package com.lms.ui.admin;

import com.lms.db.AuditLogDAO;
import com.lms.db.SystemSettingDAO;
import com.lms.model.SystemSetting;
import com.lms.ui.common.DialogHelper;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class SystemSettingsPanel extends JPanel {

    private final SystemSettingDAO settingDAO = new SystemSettingDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final JTextField txtSystemName;
    private final JSpinner spnDefaultSeats;
    private final JCheckBox chkAllowRegistration;
    private final JCheckBox chkMaintenanceMode;
    private final JTextArea txtBanner;

    public SystemSettingsPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System-Wide Configuration & Preferences");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(16, 20));

        JPanel formGrid = new JPanel(new GridLayout(4, 2, 16, 20));
        formGrid.setOpaque(false);

        txtSystemName = new JTextField();
        spnDefaultSeats = new JSpinner(new SpinnerNumberModel(50, 10, 500, 5));
        chkAllowRegistration = new JCheckBox("Enable Public Student Self-Registration on Login Screen");
        chkAllowRegistration.setOpaque(false);
        chkMaintenanceMode = new JCheckBox("Enable System Maintenance Mode (Read-Only)");
        chkMaintenanceMode.setOpaque(false);

        formGrid.add(new JLabel("LMS System Display Name:"));
        formGrid.add(txtSystemName);
        formGrid.add(new JLabel("Default Course Capacity:"));
        formGrid.add(spnDefaultSeats);
        formGrid.add(new JLabel("Self-Registration:"));
        formGrid.add(chkAllowRegistration);
        formGrid.add(new JLabel("Maintenance Mode:"));
        formGrid.add(chkMaintenanceMode);

        card.add(formGrid, BorderLayout.NORTH);

        // Announcement Banner
        JPanel bannerPanel = new JPanel(new BorderLayout(0, 8));
        bannerPanel.setOpaque(false);
        bannerPanel.add(new JLabel("Global System Announcement Banner (Shown on top of dashboards):"), BorderLayout.NORTH);

        txtBanner = new JTextArea(4, 30);
        txtBanner.setLineWrap(true);
        txtBanner.setWrapStyleWord(true);
        JScrollPane scrollBanner = new JScrollPane(txtBanner);
        scrollBanner.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        bannerPanel.add(scrollBanner, BorderLayout.CENTER);

        card.add(bannerPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setOpaque(false);

        JButton btnReset = UITheme.createSecondaryButton("Reload Settings");
        btnReset.addActionListener(e -> loadSettings());

        JButton btnSave = UITheme.createPrimaryButton("Save System Settings");
        btnSave.addActionListener(e -> saveSettings());

        btnPanel.add(btnReset);
        btnPanel.add(btnSave);
        card.add(btnPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        loadSettings();
    }

    public void loadSettings() {
        try {
            txtSystemName.setText(settingDAO.getValue("system_name", "NextGen Enterprise LMS"));
            int defaultSeats = Integer.parseInt(settingDAO.getValue("max_course_capacity_default", "50"));
            spnDefaultSeats.setValue(defaultSeats);
            boolean allowReg = Boolean.parseBoolean(settingDAO.getValue("allow_self_registration", "true"));
            chkAllowRegistration.setSelected(allowReg);
            boolean maint = Boolean.parseBoolean(settingDAO.getValue("maintenance_mode", "false"));
            chkMaintenanceMode.setSelected(maint);
            txtBanner.setText(settingDAO.getValue("notification_banner", "Welcome to the Online Learning Management System"));
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load system settings: " + e.getMessage());
        }
    }

    private void saveSettings() {
        String sysName = txtSystemName.getText().trim();
        int seats = (Integer) spnDefaultSeats.getValue();
        boolean allowReg = chkAllowRegistration.isSelected();
        boolean maint = chkMaintenanceMode.isSelected();
        String banner = txtBanner.getText().trim();

        if (sysName.isEmpty()) {
            DialogHelper.showWarning(this, "System Name cannot be empty!");
            return;
        }

        try {
            settingDAO.save(new SystemSetting("system_name", sysName, "Display name for the LMS", "GENERAL"));
            settingDAO.save(new SystemSetting("max_course_capacity_default", String.valueOf(seats), "Default course capacity", "ENROLLMENT"));
            settingDAO.save(new SystemSetting("allow_self_registration", String.valueOf(allowReg), "Self-registration toggle", "SECURITY"));
            settingDAO.save(new SystemSetting("maintenance_mode", String.valueOf(maint), "Maintenance mode toggle", "GENERAL"));
            settingDAO.save(new SystemSetting("notification_banner", banner, "Global banner text", "NOTIFICATION"));

            auditLogDAO.log(0, "ADMIN", "SETTINGS_UPDATED", "System settings updated: Name=" + sysName + ", Maintenance=" + maint);

            // Output confirmation message as required
            DialogHelper.showSuccess(this, "Confirmation: System-wide settings have been updated successfully!");
        } catch (Exception ex) {
            DialogHelper.showError(this, "Failed to save settings: " + ex.getMessage());
        }
    }
}
