package com.lms.ui.student;

import com.lms.concurrent.ConcurrentEnrollmentManager;
import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Student;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrolledCoursesPanel extends JPanel {

    private final Student currentStudent;
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    private final ModernTable tableEnrolled;
    private final DefaultTableModel tableModel;

    private List<Enrollment> enrolledList = new ArrayList<>();

    public EnrolledCoursesPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("My Enrolled Courses & Study Programs");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnView = UITheme.createPrimaryButton("👁 View Course Details");
        btnView.addActionListener(e -> viewCourseDetails());

        JButton btnDrop = UITheme.createDangerButton("✕ Drop Course");
        btnDrop.addActionListener(e -> dropCourse());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadEnrolled());

        actionBtns.add(btnView);
        actionBtns.add(btnDrop);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Enrollment ID", "Course Code", "Course Title", "Completion %", "Status", "Final Grade", "Enrolled Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableEnrolled = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableEnrolled);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        loadEnrolled();
    }

    public void loadEnrolled() {
        try {
            enrolledList = enrollmentDAO.findByStudentId(currentStudent.getId());
            tableModel.setRowCount(0);
            for (Enrollment e : enrolledList) {
                String grade = e.getFinalGrade() != null ? String.format("%.1f pts", e.getFinalGrade()) : "In Progress";
                tableModel.addRow(new Object[]{
                        e.getId(),
                        e.getCourseCode(),
                        e.getCourseTitle(),
                        e.getProgressPercent() + "%",
                        e.getStatus(),
                        grade,
                        e.getFormattedEnrolledAt()
                });
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load enrolled courses: " + e.getMessage());
        }
    }

    private void viewCourseDetails() {
        int row = tableEnrolled.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select an enrolled course from the table.");
            return;
        }

        Enrollment e = enrolledList.get(row);
        try {
            Optional<Course> cOpt = courseDAO.findById(e.getCourseId());
            if (!cOpt.isPresent()) return;
            Course c = cOpt.get();

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), c.getTitle(), true);
            dlg.setSize(500, 440);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());

            JTextArea txt = new JTextArea();
            txt.setEditable(false);
            txt.setLineWrap(true);
            txt.setWrapStyleWord(true);
            txt.setFont(UITheme.FONT_BODY);
            txt.setText(String.format("COURSE: %s (%s)\nINSTRUCTOR: %s\nCATEGORY: %s\n\nCURRENT PROGRESS: %d%%\nSTATUS: %s\n\nDESCRIPTION:\n%s\n\nSYLLABUS & MODULES:\n%s",
                    c.getTitle(), c.getCode(), c.getInstructorName(), c.getCategory(),
                    e.getProgressPercent(), e.getStatus(),
                    c.getDescription() != null ? c.getDescription() : "None",
                    c.getSyllabus() != null ? c.getSyllabus() : "None"));

            JScrollPane scroll = new JScrollPane(txt);
            scroll.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            dlg.add(scroll, BorderLayout.CENTER);

            JButton btnClose = UITheme.createPrimaryButton("Close");
            btnClose.addActionListener(ev -> dlg.dispose());
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            p.add(btnClose);
            dlg.add(p, BorderLayout.SOUTH);

            dlg.setVisible(true);
        } catch (Exception ex) {
            DialogHelper.showError(this, "Failed to load course details: " + ex.getMessage());
        }
    }

    private void dropCourse() {
        int row = tableEnrolled.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select a course to drop.");
            return;
        }

        Enrollment e = enrolledList.get(row);
        if (DialogHelper.confirm(this, "Are you sure you want to drop '" + e.getCourseTitle() + "'?")) {
            try {
                ConcurrentEnrollmentManager.getInstance().dropCourse(
                        e.getId(),
                        e.getCourseId(),
                        currentStudent.getId(),
                        currentStudent.getName()
                );
                DialogHelper.showSuccess(this, "Confirmation: Successfully dropped " + e.getCourseTitle());
                loadEnrolled();
            } catch (Exception ex) {
                DialogHelper.showError(this, "Failed to drop course: " + ex.getMessage());
            }
        }
    }
}
