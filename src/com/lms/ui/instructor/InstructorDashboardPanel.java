package com.lms.ui.instructor;

import com.lms.db.AssignmentDAO;
import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Course;
import com.lms.model.Instructor;
import com.lms.model.Submission;
import com.lms.ui.common.ChartPanel;
import com.lms.ui.common.StatCard;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InstructorDashboardPanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    private final StatCard cardMyCourses;
    private final StatCard cardTotalStudents;
    private final StatCard cardPendingGrades;
    private final StatCard cardAvgProgress;

    private final ChartPanel barChart;
    private final ChartPanel donutChart;

    public InstructorDashboardPanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Welcome back, " + currentInstructor.getName());
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh Dashboard");
        btnRefresh.addActionListener(e -> loadDashboardData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel contentPanel = new JPanel(new BorderLayout(16, 20));
        contentPanel.setOpaque(false);

        // KPI Cards Row
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        cardMyCourses = new StatCard("My Courses", "0", "Assigned Courses", UITheme.PRIMARY);
        cardTotalStudents = new StatCard("Enrolled Students", "0", "Across My Courses", UITheme.SUCCESS);
        cardPendingGrades = new StatCard("Pending Submissions", "0", "Needs Evaluation", UITheme.WARNING);
        cardAvgProgress = new StatCard("Class Avg Progress", "0%", "Active Engagement", UITheme.INFO);

        cardsGrid.add(cardMyCourses);
        cardsGrid.add(cardTotalStudents);
        cardsGrid.add(cardPendingGrades);
        cardsGrid.add(cardAvgProgress);

        contentPanel.add(cardsGrid, BorderLayout.NORTH);

        // Visualizations Row
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);

        barChart = new ChartPanel("My Courses Enrollment Capacity", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("Grading Status Overview", ChartPanel.ChartType.DONUT);

        chartsGrid.add(barChart);
        chartsGrid.add(donutChart);

        contentPanel.add(chartsGrid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadDashboardData();
    }

    public void loadDashboardData() {
        try {
            List<Course> myCourses = courseDAO.findByInstructorId(currentInstructor.getId());
            cardMyCourses.setValue(String.valueOf(myCourses.size()));

            int totalStudents = 0;
            Map<String, Integer> enrollMap = new LinkedHashMap<>();
            for (Course c : myCourses) {
                totalStudents += c.getEnrolledCount();
                enrollMap.put(c.getCode(), c.getEnrolledCount());
            }
            cardTotalStudents.setValue(String.valueOf(totalStudents));

            // Submissions count
            int pendingCount = 0;
            int gradedCount = 0;
            for (Course c : myCourses) {
                List<Submission> subs = submissionDAO.findByCourseId(c.getId());
                for (Submission s : subs) {
                    if ("PENDING".equalsIgnoreCase(s.getStatus())) pendingCount++;
                    else gradedCount++;
                }
            }
            cardPendingGrades.setValue(String.valueOf(pendingCount));

            double avgProg = enrollmentDAO.getAverageProgress();
            cardAvgProgress.setValue(String.format("%.1f%%", avgProg));

            barChart.setData(enrollMap);

            Map<String, Integer> gradeMap = new LinkedHashMap<>();
            gradeMap.put("Graded", gradedCount);
            gradeMap.put("Pending Evaluation", pendingCount);
            donutChart.setData(gradeMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
