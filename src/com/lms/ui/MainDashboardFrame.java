package com.lms.ui;

import com.lms.db.SystemSettingDAO;
import com.lms.model.Admin;
import com.lms.model.Instructor;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.ui.admin.AdminDashboardPanel;
import com.lms.ui.admin.CourseManagementPanel;
import com.lms.ui.admin.PerformanceAnalyticsPanel;
import com.lms.ui.admin.SystemActivityPanel;
import com.lms.ui.admin.SystemSettingsPanel;
import com.lms.ui.admin.UserManagementPanel;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.instructor.AssignmentGradingPanel;
import com.lms.ui.instructor.EnrollmentStatsPanel;
import com.lms.ui.instructor.FeedbackSummaryPanel;
import com.lms.ui.instructor.InstructorCoursePanel;
import com.lms.ui.instructor.InstructorDashboardPanel;
import com.lms.ui.instructor.StudentPerformancePanel;
import com.lms.ui.student.AssignmentSubmissionPanel;
import com.lms.ui.student.EnrolledCoursesPanel;
import com.lms.ui.student.GradesFeedbackPanel;
import com.lms.ui.student.MaterialViewerPanel;
import com.lms.ui.student.StudentCatalogPanel;
import com.lms.ui.student.StudentDashboardPanel;
import com.lms.ui.student.StudentProgressPanel;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final JPanel sidebarNav = new JPanel();
    private final List<JButton> navButtons = new ArrayList<>();

    public MainDashboardFrame(User user) {
        this.currentUser = user;

        setTitle(user.getDashboardTitle() + " - NextGen LMS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // 1. Top Header Bar
        add(createTopBar(), BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        add(createSidebar(), BorderLayout.WEST);

        // 3. Center Dynamic Content
        buildRoleSpecificPanels();
        add(contentCards, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("🎓  NEXTGEN LMS");
        lblLogo.setFont(UITheme.FONT_TITLE);
        lblLogo.setForeground(UITheme.PRIMARY);
        brandPanel.add(lblLogo);

        // Announcement banner
        SystemSettingDAO settingDAO = new SystemSettingDAO();
        String announcement = settingDAO.getValue("notification_banner", "Welcome to Online Learning Management System");
        JLabel lblBanner = UITheme.createBadge("📢 " + announcement, UITheme.PRIMARY_LIGHT, UITheme.PRIMARY);
        brandPanel.add(lblBanner);

        topBar.add(brandPanel, BorderLayout.WEST);

        // Right User Info & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel(currentUser.getName() + " (" + currentUser.getEmail() + ")");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.TEXT_PRIMARY);

        Color badgeBg = currentUser.getRole().name().equals("ADMIN") ? UITheme.DANGER_LIGHT :
                (currentUser.getRole().name().equals("INSTRUCTOR") ? UITheme.INFO_LIGHT : UITheme.SUCCESS_LIGHT);
        Color badgeFg = currentUser.getRole().name().equals("ADMIN") ? UITheme.DANGER :
                (currentUser.getRole().name().equals("INSTRUCTOR") ? UITheme.INFO : UITheme.SUCCESS);
        JLabel lblRoleBadge = UITheme.createBadge(currentUser.getRoleName().toUpperCase(), badgeBg, badgeFg);

        JButton btnLogout = UITheme.createSecondaryButton("Switch Role / Logout");
        btnLogout.addActionListener(e -> {
            if (DialogHelper.confirm(this, "Are you sure you want to log out?")) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        userPanel.add(lblUser);
        userPanel.add(lblRoleBadge);
        userPanel.add(btnLogout);

        topBar.add(userPanel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, getHeight()));

        sidebarNav.setLayout(new BoxLayout(sidebarNav, BoxLayout.Y_AXIS));
        sidebarNav.setOpaque(false);
        sidebarNav.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JScrollPane scroll = new JScrollPane(sidebarNav);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        sidebar.add(scroll, BorderLayout.CENTER);

        // Footer version info
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));
        JLabel lblVer = new JLabel("LMS Platform v2.4 • OOP & JDBC");
        lblVer.setFont(UITheme.FONT_SMALL);
        lblVer.setForeground(UITheme.TEXT_MUTED);
        footer.add(lblVer);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(String title, String cardKey, boolean isDefault) {
        JButton btn = new JButton(title);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(isDefault ? UITheme.SIDEBAR_ACTIVE : UITheme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(216, 42));
        btn.setPreferredSize(new Dimension(216, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addActionListener(e -> {
            cardLayout.show(contentCards, cardKey);
            for (JButton b : navButtons) {
                b.setBackground(UITheme.SIDEBAR_BG);
            }
            btn.setBackground(UITheme.SIDEBAR_ACTIVE);
        });

        navButtons.add(btn);
        sidebarNav.add(btn);
        sidebarNav.add(Box.createVerticalStrut(6));
    }

    private void buildRoleSpecificPanels() {
        switch (currentUser.getRole()) {
            case ADMIN:
                buildAdminDashboard();
                break;
            case INSTRUCTOR:
                buildInstructorDashboard((Instructor) currentUser);
                break;
            case STUDENT:
            default:
                buildStudentDashboard((Student) currentUser);
                break;
        }
    }

    private void buildAdminDashboard() {
        AdminDashboardPanel overview = new AdminDashboardPanel();
        UserManagementPanel users = new UserManagementPanel();
        CourseManagementPanel courses = new CourseManagementPanel();
        PerformanceAnalyticsPanel analytics = new PerformanceAnalyticsPanel();
        SystemSettingsPanel settings = new SystemSettingsPanel();
        SystemActivityPanel activity = new SystemActivityPanel();

        contentCards.add(overview, "ADMIN_OVERVIEW");
        contentCards.add(users, "ADMIN_USERS");
        contentCards.add(courses, "ADMIN_COURSES");
        contentCards.add(analytics, "ADMIN_ANALYTICS");
        contentCards.add(settings, "ADMIN_SETTINGS");
        contentCards.add(activity, "ADMIN_ACTIVITY");

        addNavButton("📊  Overview Dashboard", "ADMIN_OVERVIEW", true);
        addNavButton("👥  User Management", "ADMIN_USERS", false);
        addNavButton("📚  Course Management", "ADMIN_COURSES", false);
        addNavButton("📈  Performance Analytics", "ADMIN_ANALYTICS", false);
        addNavButton("⚙  System Settings", "ADMIN_SETTINGS", false);
        addNavButton("⚡  Live Activity Monitor", "ADMIN_ACTIVITY", false);
    }

    private void buildInstructorDashboard(Instructor instructor) {
        InstructorDashboardPanel overview = new InstructorDashboardPanel(instructor);
        InstructorCoursePanel courses = new InstructorCoursePanel(instructor);
        AssignmentGradingPanel grading = new AssignmentGradingPanel(instructor);
        StudentPerformancePanel performance = new StudentPerformancePanel(instructor);
        EnrollmentStatsPanel stats = new EnrollmentStatsPanel(instructor);
        FeedbackSummaryPanel feedback = new FeedbackSummaryPanel(instructor);

        contentCards.add(overview, "INST_OVERVIEW");
        contentCards.add(courses, "INST_COURSES");
        contentCards.add(grading, "INST_GRADING");
        contentCards.add(performance, "INST_PERFORMANCE");
        contentCards.add(stats, "INST_STATS");
        contentCards.add(feedback, "INST_FEEDBACK");

        addNavButton("📊  Dashboard Overview", "INST_OVERVIEW", true);
        addNavButton("📚  My Courses & Content", "INST_COURSES", false);
        addNavButton("✎  Assignment Grading", "INST_GRADING", false);
        addNavButton("👥  Student Performance", "INST_PERFORMANCE", false);
        addNavButton("📈  Enrollment Statistics", "INST_STATS", false);
        addNavButton("💬  Feedback Summary", "INST_FEEDBACK", false);
    }

    private void buildStudentDashboard(Student student) {
        StudentDashboardPanel overview = new StudentDashboardPanel(student);
        StudentCatalogPanel catalog = new StudentCatalogPanel(student);
        EnrolledCoursesPanel enrolled = new EnrolledCoursesPanel(student);
        MaterialViewerPanel materials = new MaterialViewerPanel(student);
        AssignmentSubmissionPanel assignments = new AssignmentSubmissionPanel(student);
        StudentProgressPanel progress = new StudentProgressPanel(student);
        GradesFeedbackPanel grades = new GradesFeedbackPanel(student);

        contentCards.add(overview, "STU_OVERVIEW");
        contentCards.add(catalog, "STU_CATALOG");
        contentCards.add(enrolled, "STU_ENROLLED");
        contentCards.add(materials, "STU_MATERIALS");
        contentCards.add(assignments, "STU_ASSIGNMENTS");
        contentCards.add(progress, "STU_PROGRESS");
        contentCards.add(grades, "STU_GRADES");

        addNavButton("📊  My Dashboard", "STU_OVERVIEW", true);
        addNavButton("🌐  Course Catalog (Enroll)", "STU_CATALOG", false);
        addNavButton("📚  My Enrolled Courses", "STU_ENROLLED", false);
        addNavButton("📁  Course Materials", "STU_MATERIALS", false);
        addNavButton("📝  Assignment Submissions", "STU_ASSIGNMENTS", false);
        addNavButton("📈  Progress Tracking", "STU_PROGRESS", false);
        addNavButton("★  Feedback, Grades & Quiz", "STU_GRADES", false);
    }
}
