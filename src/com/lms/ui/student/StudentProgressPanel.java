package com.lms.ui.student;

import com.lms.db.AuditLogDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Enrollment;
import com.lms.model.Student;
import com.lms.model.Submission;
import com.lms.ui.common.ChartPanel;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.ReportExporter;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentProgressPanel extends JPanel {

    private final Student currentStudent;
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ChartPanel barChart;
    private final ChartPanel donutChart;
    private final ModernTable tableProgress;
    private final DefaultTableModel tableModel;

    private List<Enrollment> enrolledList = new ArrayList<>();

    public StudentProgressPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Academic Progress Tracking & Module Completion");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnAdvance = UITheme.createPrimaryButton("▶ Complete Next Module (+15%)");
        btnAdvance.addActionListener(e -> advanceSelectedCourse());

        JButton btnExport = UITheme.createSecondaryButton("⤓ Export Progress Report");
        btnExport.addActionListener(e -> exportProgressReport());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadProgress());

        actionBtns.add(btnAdvance);
        actionBtns.add(btnExport);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split View
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setOpaque(false);

        // Graphs
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);
        chartsGrid.setPreferredSize(new Dimension(800, 240));

        barChart = new ChartPanel("Course Progress Completion (%)", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("Progress Milestones Breakdown", ChartPanel.ChartType.DONUT);

        chartsGrid.add(barChart);
        chartsGrid.add(donutChart);
        mainContent.add(chartsGrid, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Course Code", "Course Title", "Current Progress", "Status", "Final Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableProgress = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableProgress);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Course Completion Breakdown:"));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        loadProgress();
    }

    public void loadProgress() {
        try {
            enrolledList = enrollmentDAO.findByStudentId(currentStudent.getId());
            tableModel.setRowCount(0);

            Map<String, Integer> progMap = new LinkedHashMap<>();
            int completedCount = 0;
            int inProgressCount = 0;

            for (Enrollment e : enrolledList) {
                progMap.put(e.getCourseCode(), e.getProgressPercent());
                if (e.getProgressPercent() >= 100) completedCount++;
                else inProgressCount++;

                String grade = e.getFinalGrade() != null ? String.format("%.1f pts", e.getFinalGrade()) : "Pending";
                tableModel.addRow(new Object[]{
                        e.getId(),
                        e.getCourseCode(),
                        e.getCourseTitle(),
                        e.getProgressPercent() + "%",
                        e.getStatus(),
                        grade
                });
            }

            barChart.setData(progMap);

            Map<String, Integer> donutMap = new LinkedHashMap<>();
            donutMap.put("Completed (100%)", completedCount);
            donutMap.put("In Progress", inProgressCount);
            donutChart.setData(donutMap);

        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load progress data: " + e.getMessage());
        }
    }

    private void advanceSelectedCourse() {
        int row = tableProgress.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select an enrolled course to advance progress.");
            return;
        }

        Enrollment e = enrolledList.get(row);
        int currentProg = e.getProgressPercent();
        if (currentProg >= 100) {
            DialogHelper.showSuccess(this, "Congratulations! You have already achieved 100% completion in " + e.getCourseTitle() + "!");
            return;
        }

        int newProg = Math.min(100, currentProg + 15);
        try {
            enrollmentDAO.updateProgress(e.getId(), newProg);
            auditLogDAO.log(currentStudent.getId(), currentStudent.getName(), "PROGRESS_ADVANCED",
                    "Advanced progress in " + e.getCourseTitle() + " from " + currentProg + "% to " + newProg + "%");

            DialogHelper.showSuccess(this, String.format(
                    "Great work! Module completed.\nProgress in '%s' increased to %d%%!",
                    e.getCourseTitle(), newProg));

            loadProgress();
        } catch (Exception ex) {
            DialogHelper.showError(this, "Failed to advance progress: " + ex.getMessage());
        }
    }

    private void exportProgressReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("my_academic_progress_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String[] headers = {"Course Code", "Course Title", "Progress %", "Status", "Grade"};
                ReportExporter.exportToCSV(
                        enrolledList,
                        headers,
                        Arrays.asList(
                                Enrollment::getCourseCode,
                                Enrollment::getCourseTitle,
                                e -> e.getProgressPercent() + "%",
                                Enrollment::getStatus,
                                e -> e.getFinalGrade() != null ? String.valueOf(e.getFinalGrade()) : "N/A"
                        ),
                        chooser.getSelectedFile()
                );
                DialogHelper.showSuccess(this, "Confirmation: Academic Progress Report successfully saved to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Failed to export report: " + ex.getMessage());
            }
        }
    }
}
