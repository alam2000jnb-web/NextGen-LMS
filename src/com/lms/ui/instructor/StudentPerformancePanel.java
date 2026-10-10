package com.lms.ui.instructor;

import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Instructor;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.ReportExporter;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentPerformancePanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final ModernTable tableStudents;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;

    private List<Enrollment> enrollmentsList = new ArrayList<>();

    public StudentPerformancePanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Student Performance & Progress Monitoring");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnUpdate = UITheme.createPrimaryButton("✎ Update Progress / Grade");
        btnUpdate.addActionListener(e -> updateSelectedStudent());

        JButton btnExport = UITheme.createSecondaryButton("⤓ Export Student Report");
        btnExport.addActionListener(e -> exportReport());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadEnrollments());

        actionBtns.add(btnUpdate);
        actionBtns.add(btnExport);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Search Bar
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));
        searchCard.add(new JLabel("Search Student or Course:"));
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by student name or course...");
        txtSearch.addCaretListener(e -> filterTable());
        searchCard.add(txtSearch);

        // Table
        String[] columns = {"Enrollment ID", "Student Name", "Course Title", "Course Code", "Progress %", "Final Grade", "Status", "Enrolled Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableStudents = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableStudents);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(searchCard, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        loadEnrollments();
    }

    public void loadEnrollments() {
        try {
            List<Course> myCourses = courseDAO.findByInstructorId(currentInstructor.getId());
            enrollmentsList.clear();
            for (Course c : myCourses) {
                enrollmentsList.addAll(enrollmentDAO.findByCourseId(c.getId()));
            }
            filterTable();
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load student performance data: " + e.getMessage());
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Enrollment e : enrollmentsList) {
            if (query.isEmpty() ||
                    e.getStudentName().toLowerCase().contains(query) ||
                    e.getCourseTitle().toLowerCase().contains(query)) {

                String gradeStr = e.getFinalGrade() != null ? String.format("%.1f%%", e.getFinalGrade()) : "In Progress";
                tableModel.addRow(new Object[]{
                        e.getId(),
                        e.getStudentName(),
                        e.getCourseTitle(),
                        e.getCourseCode(),
                        e.getProgressPercent() + "%",
                        gradeStr,
                        e.getStatus(),
                        e.getFormattedEnrolledAt()
                });
            }
        }
    }

    private void updateSelectedStudent() {
        int row = tableStudents.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select an enrollment record from the table.");
            return;
        }

        int enrollId = (int) tableModel.getValueAt(row, 0);
        Enrollment enroll = enrollmentsList.stream().filter(e -> e.getId() == enrollId).findFirst().orElse(null);
        if (enroll == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Progress for " + enroll.getStudentName(), true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JSpinner spnProgress = new JSpinner(new SpinnerNumberModel(enroll.getProgressPercent(), 0, 100, 5));
        double curGrade = enroll.getFinalGrade() != null ? enroll.getFinalGrade() : 85.0;
        JSpinner spnGrade = new JSpinner(new SpinnerNumberModel(curGrade, 0.0, 100.0, 1.0));
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "COMPLETED", "DROPPED"});
        cmbStatus.setSelectedItem(enroll.getStatus());

        form.add(new JLabel("Student:")); form.add(new JLabel(enroll.getStudentName()));
        form.add(new JLabel("Progress Completion (%):")); form.add(spnProgress);
        form.add(new JLabel("Final Course Grade (%):")); form.add(spnGrade);
        form.add(new JLabel("Enrollment Status:")); form.add(cmbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Save Updates");
        btnSave.addActionListener(e -> {
            try {
                int newProg = (Integer) spnProgress.getValue();
                double newGrade = (Double) spnGrade.getValue();
                String newStatus = (String) cmbStatus.getSelectedItem();

                enroll.setProgressPercent(newProg);
                enroll.setFinalGrade(newGrade);
                enroll.setStatus(newStatus);

                enrollmentDAO.update(enroll);
                DialogHelper.showSuccess(this, "Confirmation: Student performance updated successfully!");
                dlg.dispose();
                loadEnrollments();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to update performance: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("student_performance_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String[] headers = {"Enrollment ID", "Student Name", "Course Title", "Course Code", "Progress %", "Final Grade", "Status"};
                ReportExporter.exportToCSV(
                        enrollmentsList,
                        headers,
                        Arrays.asList(
                                e -> String.valueOf(e.getId()),
                                Enrollment::getStudentName,
                                Enrollment::getCourseTitle,
                                Enrollment::getCourseCode,
                                e -> e.getProgressPercent() + "%",
                                e -> e.getFinalGrade() != null ? String.valueOf(e.getFinalGrade()) : "N/A",
                                Enrollment::getStatus
                        ),
                        chooser.getSelectedFile()
                );
                DialogHelper.showSuccess(this, "Student performance report exported to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Failed to export report: " + ex.getMessage());
            }
        }
    }
}
