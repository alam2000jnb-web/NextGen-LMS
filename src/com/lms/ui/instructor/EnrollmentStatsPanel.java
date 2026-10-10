package com.lms.ui.instructor;

import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Instructor;
import com.lms.ui.common.ChartPanel;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentStatsPanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final ChartPanel barChart;
    private final ChartPanel donutChart;
    private final ModernTable tableStats;
    private final DefaultTableModel tableModel;

    public EnrollmentStatsPanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course Enrollment Statistics & Engagement Reports");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createPrimaryButton("↻ Refresh Stats");
        btnRefresh.addActionListener(e -> loadStats());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setOpaque(false);

        // Graphs
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);
        chartsGrid.setPreferredSize(new Dimension(800, 250));

        barChart = new ChartPanel("Enrolled Students per Course", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("Course Enrollment Distribution", ChartPanel.ChartType.DONUT);

        chartsGrid.add(barChart);
        chartsGrid.add(donutChart);
        mainContent.add(chartsGrid, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course Code", "Course Title", "Category", "Enrolled", "Max Seats", "Fill Rate (%)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableStats = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableStats);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Course Engagement & Enrollment Summary:"));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        loadStats();
    }

    public void loadStats() {
        try {
            List<Course> courses = courseDAO.findByInstructorId(currentInstructor.getId());
            tableModel.setRowCount(0);

            Map<String, Integer> barData = new LinkedHashMap<>();
            Map<String, Integer> donutData = new LinkedHashMap<>();

            for (Course c : courses) {
                double fillRate = c.getMaxSeats() > 0 ? ((double) c.getEnrolledCount() / c.getMaxSeats()) * 100.0 : 0.0;
                barData.put(c.getCode(), c.getEnrolledCount());
                donutData.put(c.getTitle(), c.getEnrolledCount());

                tableModel.addRow(new Object[]{
                        c.getCode(),
                        c.getTitle(),
                        c.getCategory(),
                        c.getEnrolledCount(),
                        c.getMaxSeats(),
                        String.format("%.1f%%", fillRate),
                        c.getStatus()
                });
            }

            barChart.setData(barData);
            donutChart.setData(donutData);

        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load enrollment stats: " + e.getMessage());
        }
    }
}
