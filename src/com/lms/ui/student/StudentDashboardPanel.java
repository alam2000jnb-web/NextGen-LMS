package com.lms.ui.student;

import com.lms.db.AssignmentDAO;
import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Student;
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

public class StudentDashboardPanel extends JPanel {

    private final Student currentStudent;
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    private final StatCard cardEnrolled;
    private final StatCard cardAvgProgress;
    private final StatCard cardCompleted;
    private final StatCard cardAvailable;

    private final ChartPanel barChart;
    private final ChartPanel donutChart;

    public StudentDashboardPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Welcome, " + currentStudent.getName() + " (" + currentStudent.getMajor() + ")");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh Portal");
        btnRefresh.addActionListener(e -> loadStudentData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(16, 20));
        contentPanel.setOpaque(false);

        // Cards Row
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        cardEnrolled = new StatCard("Enrolled Courses", "0", "Active Semesters", UITheme.PRIMARY);
        cardAvgProgress = new StatCard("Average Progress", "0%", "Syllabus Completed", UITheme.SUCCESS);
        cardCompleted = new StatCard("Submissions", "0", "Assignments Done", UITheme.INFO);
        cardAvailable = new StatCard("Catalog Courses", "0", "Open for Enrollment", UITheme.WARNING);

        cardsGrid.add(cardEnrolled);
        cardsGrid.add(cardAvgProgress);
        cardsGrid.add(cardCompleted);
        cardsGrid.add(cardAvailable);

        contentPanel.add(cardsGrid, BorderLayout.NORTH);

        // Charts Row
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);

        barChart = new ChartPanel("My Course Progress Completion (%)", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("My Submission Evaluation Breakdown", ChartPanel.ChartType.DONUT);

        chartsGrid.add(barChart);
        chartsGrid.add(donutChart);

        contentPanel.add(chartsGrid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadStudentData();
    }

    public void loadStudentData() {
        try {
            List<Enrollment> enrollments = enrollmentDAO.findByStudentId(currentStudent.getId());
            cardEnrolled.setValue(String.valueOf(enrollments.size()));

            double avgProg = 0.0;
            Map<String, Integer> progMap = new LinkedHashMap<>();
            for (Enrollment e : enrollments) {
                avgProg += e.getProgressPercent();
                progMap.put(e.getCourseCode(), e.getProgressPercent());
            }
            if (!enrollments.isEmpty()) avgProg /= enrollments.size();
            cardAvgProgress.setValue(String.format("%.1f%%", avgProg));

            List<Submission> subs = submissionDAO.findByStudentId(currentStudent.getId());
            cardCompleted.setValue(String.valueOf(subs.size()));

            long totalCourses = courseDAO.count();
            cardAvailable.setValue(String.valueOf(totalCourses));

            barChart.setData(progMap);

            int graded = 0;
            int pending = 0;
            for (Submission s : subs) {
                if (s.isGraded()) graded++;
                else pending++;
            }
            Map<String, Integer> subBreakdown = new LinkedHashMap<>();
            subBreakdown.put("Graded", graded);
            subBreakdown.put("Pending Evaluation", pending);
            donutChart.setData(subBreakdown);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
