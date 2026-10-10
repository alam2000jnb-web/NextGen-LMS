package com.lms.ui.student;

import com.lms.concurrent.ConcurrentEnrollmentManager;
import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.exception.EnrollmentException;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Student;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class StudentCatalogPanel extends JPanel {

    private final Student currentStudent;
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final ModernTable tableCatalog;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbCategory;
    private final JTextArea txtSyllabusPreview;

    private List<Course> catalogList = new ArrayList<>();

    public StudentCatalogPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course Catalog & Academic Enrollment");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnEnroll = UITheme.createPrimaryButton("✓ Enroll in Selected Course");
        btnEnroll.addActionListener(e -> enrollInSelectedCourse());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnEnroll, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));

        filterCard.add(new JLabel("Search Catalog:"));
        txtSearch = new JTextField(18);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by title or code...");
        txtSearch.addCaretListener(e -> filterTable());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"All Categories", "Core CS", "Software Eng", "Data Science", "Databases"});
        cmbCategory.addActionListener(e -> filterTable());
        filterCard.add(cmbCategory);

        JButton btnReset = UITheme.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            loadCatalog();
        });
        filterCard.add(btnReset);

        // Table
        String[] columns = {"ID", "Code", "Course Title", "Instructor", "Category", "Seats Available", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCatalog = new ModernTable(tableModel);

        // Syllabus preview
        txtSyllabusPreview = new JTextArea(6, 20);
        txtSyllabusPreview.setEditable(false);
        txtSyllabusPreview.setLineWrap(true);
        txtSyllabusPreview.setWrapStyleWord(true);
        txtSyllabusPreview.setFont(UITheme.FONT_BODY);
        txtSyllabusPreview.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane previewScroll = new JScrollPane(txtSyllabusPreview);
        previewScroll.setBorder(BorderFactory.createTitledBorder("Selected Course Overview & Syllabus:"));
        previewScroll.setPreferredSize(new Dimension(800, 140));

        tableCatalog.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableCatalog.getSelectedRow();
                if (row >= 0 && row < catalogList.size()) {
                    Course c = catalogList.get(row);
                    txtSyllabusPreview.setText(String.format("COURSE: %s (%s)\nINSTRUCTOR: %s\n\nDESCRIPTION:\n%s\n\nSYLLABUS & MODULES:\n%s",
                            c.getTitle(), c.getCode(), c.getInstructorName(),
                            c.getDescription() != null ? c.getDescription() : "None",
                            c.getSyllabus() != null ? c.getSyllabus() : "None"));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableCatalog);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(previewScroll, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        loadCatalog();
    }

    public void loadCatalog() {
        try {
            catalogList = courseDAO.findAll();
            filterTable();
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load course catalog: " + e.getMessage());
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbCategory.getSelectedItem();

        tableModel.setRowCount(0);
        for (Course c : catalogList) {
            boolean matchesSearch = query.isEmpty() ||
                    c.getTitle().toLowerCase().contains(query) ||
                    c.getCode().toLowerCase().contains(query);

            boolean matchesCat = cat == null ||
                    cat.equals("All Categories") ||
                    c.getCategory().equalsIgnoreCase(cat);

            if (matchesSearch && matchesCat) {
                int availSeats = c.getAvailableSeats();
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCode(),
                        c.getTitle(),
                        c.getInstructorName(),
                        c.getCategory(),
                        availSeats + " / " + c.getMaxSeats(),
                        c.isFull() ? "FULL" : "ACTIVE"
                });
            }
        }
    }

    private void enrollInSelectedCourse() {
        int row = tableCatalog.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select a course to enroll from the catalog.");
            return;
        }

        int courseId = (int) tableModel.getValueAt(row, 0);
        Course course = catalogList.stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
        if (course == null) return;

        if (DialogHelper.confirm(this, "Confirm Enrollment in:\n" + course.getTitle() + " (" + course.getCode() + ")?")) {
            try {
                // Thread-safe synchronized enrollment
                Enrollment enrollment = ConcurrentEnrollmentManager.getInstance().enrollStudent(
                        currentStudent.getId(),
                        currentStudent.getName(),
                        course.getId()
                );

                // Output confirmation message as required
                DialogHelper.showSuccess(this, String.format(
                        "Confirmation: Enrollment Successful!\nYou are now officially enrolled in '%s' (%s).\nGo to 'My Enrolled Courses' or 'Course Materials' to start learning!",
                        course.getTitle(), course.getCode()));

                loadCatalog();
            } catch (EnrollmentException ee) {
                DialogHelper.showWarning(this, ee.getMessage());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Enrollment error: " + ex.getMessage());
            }
        }
    }
}
