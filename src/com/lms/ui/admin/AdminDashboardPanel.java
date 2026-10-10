package com.lms.ui.admin;

import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.UserDAO;
import com.lms.model.UserRole;
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
import java.util.Map;

public class AdminDashboardPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final StatCard cardUsers;
    private final StatCard cardCourses;
    private final StatCard cardEnrollments;
    private final StatCard cardCompletion;

    private final ChartPanel barChart;
    private final ChartPanel donutChart;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Administration Overview");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh Metrics");
        btnRefresh.addActionListener(e -> loadData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Cards + Charts
        JPanel contentPanel = new JPanel(new BorderLayout(16, 20));
        contentPanel.setOpaque(false);

        // KPI Cards Row
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        cardUsers = new StatCard("Total Users", "0", "Students & Instructors", UITheme.PRIMARY);
        cardCourses = new StatCard("Active Courses", "0", "Published Modules", UITheme.SUCCESS);
        cardEnrollments = new StatCard("Enrollments", "0", "Active Students", UITheme.INFO);
        cardCompletion = new StatCard("Avg Completion", "0%", "System-wide Progress", UITheme.WARNING);

        cardsGrid.add(cardUsers);
        cardsGrid.add(cardCourses);
        cardsGrid.add(cardEnrollments);
        cardsGrid.add(cardCompletion);

        contentPanel.add(cardsGrid, BorderLayout.NORTH);

        // Visual Charts Row
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsGrid.setOpaque(false);

        barChart = new ChartPanel("Course Enrollment Distribution", ChartPanel.ChartType.BAR);
        donutChart = new ChartPanel("User Roles Composition", ChartPanel.ChartType.DONUT);

        chartsGrid.add(barChart);
        chartsGrid.add(donutChart);

        contentPanel.add(chartsGrid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        try {
            long totalUsers = userDAO.count();
            long totalCourses = courseDAO.count();
            long totalEnrollments = enrollmentDAO.count();
            double avgProgress = enrollmentDAO.getAverageProgress();

            cardUsers.setValue(String.valueOf(totalUsers));
            cardCourses.setValue(String.valueOf(totalCourses));
            cardEnrollments.setValue(String.valueOf(totalEnrollments));
            cardCompletion.setValue(String.format("%.1f%%", avgProgress));

            // Bar chart: Course enrollments
            Map<String, Integer> enrollMap = enrollmentDAO.getEnrollmentsPerCourseMap();
            barChart.setData(enrollMap);

            // Donut chart: Role distribution
            Map<String, Long> roleMap = new LinkedHashMap<>();
            roleMap.put("Students", userDAO.countByRole(UserRole.STUDENT));
            roleMap.put("Instructors", userDAO.countByRole(UserRole.INSTRUCTOR));
            roleMap.put("Admins", userDAO.countByRole(UserRole.ADMIN));
            donutChart.setData(roleMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
