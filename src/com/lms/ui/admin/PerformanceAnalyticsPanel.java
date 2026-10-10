package com.lms.ui.admin;

import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.model.Course;
import com.lms.model.Enrollment;
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

public class PerformanceAnalyticsPanel extends JPanel {

    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final ModernTable tableMetrics;
    private final DefaultTableModel tableModel;
    private final ChartPanel barChart;
    private final ChartPanel donutChart;

    private List<Course> courses = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();

    public PerformanceAnalyticsPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Performance Analytics & Reports");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel exportBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportBtns.setOpaque(false);

        JButton btnExportCSV = UITheme.createSecondaryButton("⤓ Export CSV Report");
        btnExportCSV.addActionListener(e -> exportCSV());

        JButton btnExportTXT = UITheme.createSecondaryButton("⤓ Export Summary (TXT)");
        btnExportTXT.addActionListener(e -> exportTXT());

        JButton btnRefresh = UITheme.createPrimaryButton("↻ Refresh Analytics");
        btnRefresh.addActionListener(e -> loadAnalytics());

        exportBtns.add(btnExportCSV);
        exportBtns.add(btnExportTXT);
        exportBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(exportBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setOpaque(false);

        // Visualizations Panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setPreferredSize(new Dimension(800, 250));

        barChart = new ChartPanel("Course Capacity Utilization (%)", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("Course Enrollment Share", ChartPanel.ChartType.DONUT);

        chartsPanel.add(barChart);
        chartsPanel.add(donutChart);
        mainContent.add(chartsPanel, BorderLayout.NORTH);

        // Metrics Table
        String[] columns = {"Course Code", "Course Title", "Instructor", "Enrolled", "Max Seats", "Utilization %", "Avg Progress %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMetrics = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableMetrics);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detailed Course Performance Breakdown:"));

        mainContent.add(scrollPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        loadAnalytics();
    }

    public void loadAnalytics() {
        try {
            courses = courseDAO.findAll();
            enrollments = enrollmentDAO.findAll();

            tableModel.setRowCount(0);
            Map<String, Double> utilizationMap = new LinkedHashMap<>();
            Map<String, Integer> shareMap = new LinkedHashMap<>();

            for (Course c : courses) {
                double util = c.getMaxSeats() > 0 ? ((double) c.getEnrolledCount() / c.getMaxSeats()) * 100.0 : 0.0;
                utilizationMap.put(c.getCode(), util);
                shareMap.put(c.getTitle(), c.getEnrolledCount());

                // Calculate average progress for this course
                double avgProg = enrollments.stream()
                        .filter(e -> e.getCourseId() == c.getId())
                        .mapToInt(Enrollment::getProgressPercent)
                        .average()
                        .orElse(0.0);

                tableModel.addRow(new Object[]{
                        c.getCode(),
                        c.getTitle(),
                        c.getInstructorName(),
                        c.getEnrolledCount(),
                        c.getMaxSeats(),
                        String.format("%.1f%%", util),
                        String.format("%.1f%%", avgProg)
                });
            }

            barChart.setData(utilizationMap);
            donutChart.setData(shareMap);

        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to compute performance analytics: " + e.getMessage());
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("lms_performance_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String[] headers = {"Course Code", "Course Title", "Instructor", "Enrolled", "Capacity", "Status"};
                ReportExporter.exportToCSV(
                        courses,
                        headers,
                        Arrays.asList(
                                Course::getCode,
                                Course::getTitle,
                                Course::getInstructorName,
                                c -> String.valueOf(c.getEnrolledCount()),
                                c -> String.valueOf(c.getMaxSeats()),
                                Course::getStatus
                        ),
                        chooser.getSelectedFile()
                );
                DialogHelper.showSuccess(this, "Performance Analytics CSV successfully exported to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }

    private void exportTXT() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("lms_performance_summary.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<String> sections = new ArrayList<>();
                sections.add("1. SYSTEM KPI OVERVIEW\nTotal Courses: " + courses.size() +
                        "\nTotal System Enrollments: " + enrollments.size());

                StringBuilder courseBreakdown = new StringBuilder("2. DETAILED COURSE METRICS:\n");
                for (Course c : courses) {
                    double util = c.getMaxSeats() > 0 ? ((double) c.getEnrolledCount() / c.getMaxSeats()) * 100.0 : 0.0;
                    courseBreakdown.append(String.format(" - [%s] %s (Instructor: %s)\n   Enrolled: %d/%d (%.1f%% seat utilization)\n\n",
                            c.getCode(), c.getTitle(), c.getInstructorName(), c.getEnrolledCount(), c.getMaxSeats(), util));
                }
                sections.add(courseBreakdown.toString());

                ReportExporter.exportTextSummary("LMS Institutional Performance Report", sections, chooser.getSelectedFile());
                DialogHelper.showSuccess(this, "Performance summary report saved to:\n" + chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }
}
