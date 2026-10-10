package com.lms.ui;

import com.lms.db.AuditLogDAO;
import com.lms.db.UserDAO;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.ui.common.DialogHelper;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final JTextField txtEmail = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);

    public LoginFrame() {
        setTitle("NextGen Online Learning Management System - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        // Left Branding Banner
        JPanel leftBanner = new JPanel();
        leftBanner.setBackground(UITheme.SIDEBAR_BG);
        leftBanner.setPreferredSize(new Dimension(320, 620));
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(BorderFactory.createEmptyBorder(40, 28, 40, 28));

        JLabel lblTitle = new JLabel("<html><font color='#818CF8'>🎓 NextGen</font><br><font color='#FFFFFF'>Learning</font><br><font color='#94A3B8'>Management System</font></html>");
        lblTitle.setFont(UITheme.FONT_TITLE);

        JLabel lblSub = new JLabel("<html><br><font color='#CBD5E1'>An enterprise-grade LMS platform featuring dedicated role dashboards for Administrators, Instructors, and Students.</font><br><br><font color='#64748B'>• Java 26 & Swing UI<br>• JDBC Database Connectivity<br>• Multithreading & Sync<br>• OOP & Collections</font></html>");
        lblSub.setFont(UITheme.FONT_BODY);

        leftBanner.add(lblTitle);
        leftBanner.add(lblSub);
        leftBanner.add(Box.createVerticalGlue());

        JLabel lblFooter = new JLabel("<html><font color='#475569'>GUVI Certified Project<br>© 2024 LMS Platform</font></html>");
        lblFooter.setFont(UITheme.FONT_SMALL);
        leftBanner.add(lblFooter);

        add(leftBanner, BorderLayout.WEST);

        // Right Tabs (Sign In / Register / Quick Demos)
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_HEADER);

        tabs.addTab("Sign In", createSignInPanel());
        tabs.addTab("Quick Demo Access", createQuickDemoPanel());
        tabs.addTab("Student Registration", createRegistrationPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        JPanel form = new JPanel(new GridLayout(6, 1, 0, 8));
        form.setOpaque(false);

        JLabel lblHeading = new JLabel("Welcome Back");
        lblHeading.setFont(UITheme.FONT_TITLE);
        lblHeading.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblPrompt = new JLabel("Enter your institutional credentials to log in:");
        lblPrompt.setFont(UITheme.FONT_BODY);
        lblPrompt.setForeground(UITheme.TEXT_SECONDARY);

        form.add(lblHeading);
        form.add(lblPrompt);

        form.add(new JLabel("Email Address:"));
        txtEmail.setFont(UITheme.FONT_BODY);
        txtEmail.setText("admin@lms.edu");
        form.add(txtEmail);

        form.add(new JLabel("Password:"));
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setText("admin123");
        form.add(txtPassword);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        btnPanel.setOpaque(false);

        JButton btnLogin = UITheme.createPrimaryButton("Sign In to Portal →");
        btnLogin.setPreferredSize(new Dimension(220, 42));
        btnLogin.addActionListener(e -> attemptLogin());
        btnPanel.add(btnLogin);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(btnPanel, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createQuickDemoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("1-Click Instant Evaluation Logins");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Test any role dashboard immediately without entering credentials:");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);
        header.add(lblTitle);
        header.add(lblSub);
        panel.add(header, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(3, 1, 0, 14));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Admin Demo Card
        cardsPanel.add(createDemoRoleCard(
                "🛡️ Administrator Role (Dr. Sarah Admin)",
                "Full oversight: User Management, Course CRUD, Analytics, Settings, Live Activity.",
                "admin@lms.edu", "admin123", UITheme.DANGER
        ));

        // Instructor Demo Card
        cardsPanel.add(createDemoRoleCard(
                "👨‍🏫 Instructor Role (Prof. Alan Turing)",
                "Course creation, materials, assignment grading, student progress, enrollment stats.",
                "alan@lms.edu", "instructor123", UITheme.INFO
        ));

        // Student Demo Card
        cardsPanel.add(createDemoRoleCard(
                "🎓 Student Role (Shivam Kumar)",
                "Course catalog enrollment, view materials, submit assignments, track progress & GPA.",
                "shivam@lms.edu", "student123", UITheme.SUCCESS
        ));

        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDemoRoleCard(String title, String desc, String email, String pass, Color color) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 6));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_HEADER);
        lblT.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblD = new JLabel(desc);
        lblD.setFont(UITheme.FONT_SMALL);
        lblD.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(lblT);
        textPanel.add(lblD);

        JButton btnQuick = UITheme.createPrimaryButton("Launch " + email.split("@")[0] + " →");
        btnQuick.setBackground(color);
        btnQuick.addActionListener(e -> {
            txtEmail.setText(email);
            txtPassword.setText(pass);
            directLogin(email, pass);
        });

        card.add(textPanel, BorderLayout.CENTER);
        card.add(btnQuick, BorderLayout.EAST);
        return card;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("New Student Registration");
        lblTitle.setFont(UITheme.FONT_TITLE);
        JLabel lblSub = new JLabel("Create an online student account to start enrolling in courses:");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);
        header.add(lblTitle);
        header.add(lblSub);
        panel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(8, 1, 0, 6));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JTextField txtRegName = new JTextField();
        JTextField txtRegEmail = new JTextField();
        JPasswordField txtRegPass = new JPasswordField();
        JTextField txtRegMajor = new JTextField("Computer Science");

        form.add(new JLabel("Full Name:")); form.add(txtRegName);
        form.add(new JLabel("Email Address:")); form.add(txtRegEmail);
        form.add(new JLabel("Choose Password:")); form.add(txtRegPass);
        form.add(new JLabel("Academic Major:")); form.add(txtRegMajor);

        JButton btnRegister = UITheme.createPrimaryButton("Create Student Account");
        btnRegister.addActionListener(e -> {
            String name = txtRegName.getText().trim();
            String email = txtRegEmail.getText().trim();
            String pass = new String(txtRegPass.getPassword()).trim();
            String major = txtRegMajor.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                DialogHelper.showWarning(this, "Name, Email, and Password are required!");
                return;
            }

            try {
                Student student = new Student();
                student.setName(name);
                student.setEmail(email);
                student.setPasswordHash(pass);
                student.setMajor(major.isEmpty() ? "General Studies" : major);
                student.setRole(UserRole.STUDENT);
                student.setStatus("ACTIVE");

                userDAO.save(student);
                auditLogDAO.log(student.getId(), student.getName(), "STUDENT_REGISTRATION", "Self-registered new student: " + email);

                DialogHelper.showSuccess(this, "Registration Successful!\nYou can now log in using email: " + email);
                txtEmail.setText(email);
                txtPassword.setText(pass);
            } catch (Exception ex) {
                DialogHelper.showError(this, "Registration failed: " + ex.getMessage());
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(btnRegister, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void attemptLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        directLogin(email, pass);
    }

    private void directLogin(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            DialogHelper.showWarning(this, "Please enter email and password.");
            return;
        }

        try {
            Optional<User> userOpt = userDAO.authenticate(email, pass);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                auditLogDAO.log(user.getId(), user.getName(), "USER_LOGIN", "Signed into portal: " + user.getRoleName());
                dispose();
                new MainDashboardFrame(user).setVisible(true);
            } else {
                DialogHelper.showError(this, "Invalid credentials or account is suspended!\nPlease check your email and password.");
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Authentication system error: " + e.getMessage());
        }
    }
}
