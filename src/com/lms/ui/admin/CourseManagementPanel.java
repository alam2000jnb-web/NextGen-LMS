package com.lms.ui.admin;

import com.lms.db.AuditLogDAO;
import com.lms.db.CourseDAO;
import com.lms.db.UserDAO;
import com.lms.model.Course;
import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class CourseManagementPanel extends JPanel {

    private final CourseDAO courseDAO = new CourseDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableCourses;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbCategory;

    private List<Course> currentCourses = new ArrayList<>();
    private List<User> instructorsList = new ArrayList<>();

    public CourseManagementPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Course Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("+ Create New Course");
        btnAdd.addActionListener(e -> showCourseDialog(null));

        JButton btnEdit = UITheme.createSecondaryButton("✎ Edit Course");
        btnEdit.addActionListener(e -> {
            Course c = getSelectedCourse();
            if (c != null) showCourseDialog(c);
        });

        JButton btnDelete = UITheme.createDangerButton("✕ Delete Course");
        btnDelete.addActionListener(e -> deleteSelectedCourse());

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
        txtSearch.putClientProperty("JTextField.placeholderText", "Search course title or code...");
        txtSearch.addCaretListener(e -> filterTable());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"All Categories", "Core CS", "Software Eng", "Data Science", "Databases", "General"});
        cmbCategory.addActionListener(e -> filterTable());
        filterCard.add(cmbCategory);

        JButton btnReset = UITheme.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            loadCourses();
        });
        filterCard.add(btnReset);

        // Table
        String[] columns = {"ID", "Code", "Course Title", "Category", "Assigned Instructor", "Capacity", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tableCourses = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableCourses);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadInstructors();
        loadCourses();
    }

    private void loadInstructors() {
        try {
            instructorsList = userDAO.findByRole(UserRole.INSTRUCTOR);
        } catch (Exception ignored) {}
    }

    public void loadCourses() {
        try {
            currentCourses = courseDAO.findAll();
            populateTable(currentCourses);
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load courses: " + e.getMessage());
        }
    }

    private void populateTable(List<Course> list) {
        tableModel.setRowCount(0);
        for (Course c : list) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getCode(),
                    c.getTitle(),
                    c.getCategory(),
                    c.getInstructorName(),
                    c.getEnrolledCount() + " / " + c.getMaxSeats(),
                    c.getStatus()
            });
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbCategory.getSelectedItem();

        List<Course> filtered = new ArrayList<>();
        for (Course c : currentCourses) {
            boolean matchesSearch = query.isEmpty() ||
                    c.getTitle().toLowerCase().contains(query) ||
                    c.getCode().toLowerCase().contains(query);

            boolean matchesCat = cat == null ||
                    cat.equals("All Categories") ||
                    c.getCategory().equalsIgnoreCase(cat);

            if (matchesSearch && matchesCat) {
                filtered.add(c);
            }
        }
        populateTable(filtered);
    }

    private Course getSelectedCourse() {
        int selectedRow = tableCourses.getSelectedRow();
        if (selectedRow < 0) {
            DialogHelper.showWarning(this, "Please select a course from the table.");
            return null;
        }
        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        return currentCourses.stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
    }

    private void showCourseDialog(Course existing) {
        boolean isEdit = existing != null;
        String title = isEdit ? "Edit Course #" + existing.getId() : "Create New Course";

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JTextField txtCode = new JTextField(isEdit ? existing.getCode() : "");
        JTextField txtTitle = new JTextField(isEdit ? existing.getTitle() : "");
        JTextField txtCategory = new JTextField(isEdit ? existing.getCategory() : "Core CS");

        JComboBox<User> cmbInstructor = new JComboBox<>();
        for (User u : instructorsList) {
            cmbInstructor.addItem(u);
            if (isEdit && u.getId() == existing.getInstructorId()) {
                cmbInstructor.setSelectedItem(u);
            }
        }

        JSpinner spnSeats = new JSpinner(new SpinnerNumberModel(isEdit ? existing.getMaxSeats() : 40, 5, 500, 5));
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "ARCHIVED", "DRAFT"});
        if (isEdit) cmbStatus.setSelectedItem(existing.getStatus());

        form.add(new JLabel("Course Code:"));
        form.add(txtCode);
        form.add(new JLabel("Course Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Category:"));
        form.add(txtCategory);
        form.add(new JLabel("Lead Instructor:"));
        form.add(cmbInstructor);
        form.add(new JLabel("Max Seat Capacity:"));
        form.add(spnSeats);
        form.add(new JLabel("Course Status:"));
        form.add(cmbStatus);

        // Description & Syllabus panel
        JPanel textPanels = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanels.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));

        JTextArea txtDesc = new JTextArea(isEdit ? existing.getDescription() : "", 3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBorder(BorderFactory.createTitledBorder("Course Description:"));

        JTextArea txtSyllabus = new JTextArea(isEdit ? existing.getSyllabus() : "", 4, 20);
        txtSyllabus.setLineWrap(true);
        txtSyllabus.setWrapStyleWord(true);
        JScrollPane sylScroll = new JScrollPane(txtSyllabus);
        sylScroll.setBorder(BorderFactory.createTitledBorder("Course Syllabus / Outline:"));

        textPanels.add(descScroll);
        textPanels.add(sylScroll);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(textPanels, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton(isEdit ? "Update Course" : "Create Course");
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String cTitle = txtTitle.getText().trim();
            String category = txtCategory.getText().trim();
            User inst = (User) cmbInstructor.getSelectedItem();
            int seats = (Integer) spnSeats.getValue();
            String status = (String) cmbStatus.getSelectedItem();
            String desc = txtDesc.getText().trim();
            String syllabus = txtSyllabus.getText().trim();

            if (code.isEmpty() || cTitle.isEmpty() || inst == null) {
                DialogHelper.showWarning(dlg, "Course Code, Title, and Instructor are required!");
                return;
            }

            try {
                if (isEdit) {
                    existing.setCode(code);
                    existing.setTitle(cTitle);
                    existing.setCategory(category);
                    existing.setInstructorId(inst.getId());
                    existing.setInstructorName(inst.getName());
                    existing.setMaxSeats(seats);
                    existing.setStatus(status);
                    existing.setDescription(desc);
                    existing.setSyllabus(syllabus);

                    courseDAO.update(existing);
                    auditLogDAO.log(0, "ADMIN", "COURSE_UPDATED", "Updated course details: " + code + " - " + cTitle);
                    DialogHelper.showSuccess(this, "Confirmation: Course '" + cTitle + "' updated successfully!");
                } else {
                    Course newCourse = new Course();
                    newCourse.setCode(code);
                    newCourse.setTitle(cTitle);
                    newCourse.setCategory(category);
                    newCourse.setInstructorId(inst.getId());
                    newCourse.setInstructorName(inst.getName());
                    newCourse.setMaxSeats(seats);
                    newCourse.setStatus(status);
                    newCourse.setDescription(desc);
                    newCourse.setSyllabus(syllabus);

                    courseDAO.save(newCourse);
                    auditLogDAO.log(0, "ADMIN", "COURSE_CREATED", "Created course: " + code + " - " + cTitle);
                    DialogHelper.showSuccess(this, "Confirmation: Course '" + cTitle + "' created successfully!");
                }

                dlg.dispose();
                loadCourses();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Error saving course: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(centerPanel, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelectedCourse() {
        Course c = getSelectedCourse();
        if (c == null) return;

        if (DialogHelper.confirm(this, "Are you sure you want to delete course '" + c.getTitle() + "' (" + c.getCode() + ")?\nAll enrolled records, assignments, and materials will also be removed!")) {
            try {
                courseDAO.deleteById(c.getId());
                auditLogDAO.log(0, "ADMIN", "COURSE_DELETED", "Deleted course #" + c.getId() + " (" + c.getTitle() + ")");
                DialogHelper.showSuccess(this, "Confirmation: Course '" + c.getTitle() + "' deleted successfully!");
                loadCourses();
            } catch (Exception e) {
                DialogHelper.showError(this, "Failed to delete course: " + e.getMessage());
            }
        }
    }
}
