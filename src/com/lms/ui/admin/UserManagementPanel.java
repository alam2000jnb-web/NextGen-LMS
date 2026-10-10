package com.lms.ui.admin;

import com.lms.db.AuditLogDAO;
import com.lms.db.UserDAO;
import com.lms.model.Admin;
import com.lms.model.Instructor;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableUsers;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbRoleFilter;

    private List<User> currentUsers = new ArrayList<>();

    public UserManagementPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("User Account Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("+ Add New User");
        btnAdd.addActionListener(e -> showAddUserDialog());

        JButton btnEdit = UITheme.createSecondaryButton("✎ Edit Selected");
        btnEdit.addActionListener(e -> showEditUserDialog());

        JButton btnDelete = UITheme.createDangerButton("✕ Delete");
        btnDelete.addActionListener(e -> deleteSelectedUser());

        actionBtns.add(btnAdd);
        actionBtns.add(btnEdit);
        actionBtns.add(btnDelete);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));

        filterCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(18);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name or email...");
        txtSearch.addCaretListener(e -> filterTable());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Filter by Role:"));
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "STUDENT", "INSTRUCTOR", "ADMIN"});
        cmbRoleFilter.addActionListener(e -> filterTable());
        filterCard.add(cmbRoleFilter);

        JButton btnReset = UITheme.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbRoleFilter.setSelectedIndex(0);
            loadUsers();
        });
        filterCard.add(btnReset);

        // Table
        String[] columns = {"ID", "Full Name", "Email Address", "Role", "Status", "Dept / Major"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tableUsers = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableUsers);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadUsers();
    }

    public void loadUsers() {
        try {
            currentUsers = userDAO.findAll();
            populateTable(currentUsers);
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load users: " + e.getMessage());
        }
    }

    private void populateTable(List<User> list) {
        tableModel.setRowCount(0);
        for (User u : list) {
            String deptOrMajor = "";
            if (u instanceof Instructor) deptOrMajor = ((Instructor) u).getDepartment();
            else if (u instanceof Student) deptOrMajor = ((Student) u).getMajor();
            else if (u instanceof Admin) deptOrMajor = ((Admin) u).getDepartment();

            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.getStatus(),
                    deptOrMajor
            });
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        String selectedRole = (String) cmbRoleFilter.getSelectedItem();

        List<User> filtered = new ArrayList<>();
        for (User u : currentUsers) {
            boolean matchesSearch = query.isEmpty() ||
                    u.getName().toLowerCase().contains(query) ||
                    u.getEmail().toLowerCase().contains(query);

            boolean matchesRole = selectedRole == null ||
                    selectedRole.equals("All Roles") ||
                    u.getRole().name().equalsIgnoreCase(selectedRole);

            if (matchesSearch && matchesRole) {
                filtered.add(u);
            }
        }
        populateTable(filtered);
    }

    private void showAddUserDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New User Account", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 14));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JComboBox<UserRole> cmbRole = new JComboBox<>(UserRole.values());
        JTextField txtDept = new JTextField();

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(cmbRole);
        formPanel.add(new JLabel("Department / Major:"));
        formPanel.add(txtDept);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Create Account");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();
            UserRole role = (UserRole) cmbRole.getSelectedItem();
            String dept = txtDept.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                DialogHelper.showWarning(dlg, "Name, Email, and Password are required fields!");
                return;
            }

            try {
                User newUser;
                if (role == UserRole.ADMIN) {
                    Admin a = new Admin();
                    a.setDepartment(dept.isEmpty() ? "Administration" : dept);
                    newUser = a;
                } else if (role == UserRole.INSTRUCTOR) {
                    Instructor inst = new Instructor();
                    inst.setDepartment(dept.isEmpty() ? "Computer Science" : dept);
                    newUser = inst;
                } else {
                    Student stu = new Student();
                    stu.setMajor(dept.isEmpty() ? "Computer Science" : dept);
                    newUser = stu;
                }

                newUser.setName(name);
                newUser.setEmail(email);
                newUser.setPasswordHash(pass);
                newUser.setRole(role);
                newUser.setStatus("ACTIVE");

                userDAO.save(newUser);
                auditLogDAO.log(0, "ADMIN", "USER_CREATED", "Created user account: " + email + " (" + role + ")");

                DialogHelper.showSuccess(this, "Confirmation: User account '" + name + "' created successfully!");
                dlg.dispose();
                loadUsers();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to create user: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(formPanel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditUserDialog() {
        int selectedRow = tableUsers.getSelectedRow();
        if (selectedRow < 0) {
            DialogHelper.showWarning(this, "Please select a user to edit from the table.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        User user = currentUsers.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
        if (user == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User Account #" + userId, true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 14));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField txtName = new JTextField(user.getName());
        JTextField txtEmail = new JTextField(user.getEmail());
        JPasswordField txtPassword = new JPasswordField(user.getPasswordHash());
        JComboBox<UserRole> cmbRole = new JComboBox<>(UserRole.values());
        cmbRole.setSelectedItem(user.getRole());

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "SUSPENDED"});
        cmbStatus.setSelectedItem(user.getStatus());

        String currentExtra = "";
        if (user instanceof Instructor) currentExtra = ((Instructor) user).getDepartment();
        else if (user instanceof Student) currentExtra = ((Student) user).getMajor();
        JTextField txtDept = new JTextField(currentExtra);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(cmbRole);
        formPanel.add(new JLabel("Account Status:"));
        formPanel.add(cmbStatus);
        formPanel.add(new JLabel("Department / Major:"));
        formPanel.add(txtDept);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Save Changes");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();
            UserRole role = (UserRole) cmbRole.getSelectedItem();
            String status = (String) cmbStatus.getSelectedItem();
            String dept = txtDept.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                DialogHelper.showWarning(dlg, "Name and Email cannot be empty!");
                return;
            }

            try {
                user.setName(name);
                user.setEmail(email);
                if (!pass.isEmpty()) user.setPasswordHash(pass);
                user.setRole(role);
                user.setStatus(status);

                if (user instanceof Instructor) ((Instructor) user).setDepartment(dept);
                else if (user instanceof Student) ((Student) user).setMajor(dept);

                userDAO.update(user);
                auditLogDAO.log(0, "ADMIN", "USER_UPDATED", "Updated user details for #" + userId + " (" + email + ")");

                DialogHelper.showSuccess(this, "Confirmation: User account #" + userId + " updated successfully!");
                dlg.dispose();
                loadUsers();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to update user: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(formPanel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelectedUser() {
        int selectedRow = tableUsers.getSelectedRow();
        if (selectedRow < 0) {
            DialogHelper.showWarning(this, "Please select a user to delete.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        if (userId == 1) {
            DialogHelper.showError(this, "Cannot delete primary root Administrator account!");
            return;
        }

        if (DialogHelper.confirm(this, "Are you sure you want to permanently delete user '" + name + "' (ID #" + userId + ")?")) {
            try {
                userDAO.deleteById(userId);
                auditLogDAO.log(0, "ADMIN", "USER_DELETED", "Deleted user #" + userId + " (" + name + ")");
                DialogHelper.showSuccess(this, "Confirmation: User account '" + name + "' deleted successfully!");
                loadUsers();
            } catch (Exception e) {
                DialogHelper.showError(this, "Failed to delete user: " + e.getMessage());
            }
        }
    }
}
